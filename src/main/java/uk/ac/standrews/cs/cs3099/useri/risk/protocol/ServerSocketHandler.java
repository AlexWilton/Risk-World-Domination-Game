package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

import org.json.simple.JSONArray;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.NetworkClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.webClient.WebClient;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Map;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.HashMismatchException;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.RandomNumberGenerator;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.InitialiseGameCommand;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.exceptions.InitialisationException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * main network communication thread.
 * handles sending and receiving messages
 */
public class ServerSocketHandler implements Runnable {
    private final int PORT, NUMBER_OF_PLAYERS, ACK_TIMEOUT = 30, MOVE_TIMEOUT = 30000;
    public static final int MAX_PLAYER_COUNT = 6, MIN_PLAYER_COUNT = 2; //needed for web client to know the range of allowed number of players. (needs to be public)
    private WebClient webClient;
    private ServerSocket server;
    private ArrayList<ListenerThread> clientSocketPool;
    private ArrayList<Thread> threads = new ArrayList<>();
    private boolean isServerPlaying;
    private RejectingThread reject;

    private boolean gameInProgress = false;
    private RandomNumberGenerator seed;

    /**
     * getter of all the connected player names
     * @return String ArrayList to return all the player names
     */
    public ArrayList<String> getConnectedPlayerNames(){
        ArrayList<String> ret = new ArrayList<>();
        for (ListenerThread t : clientSocketPool){
            ret.add(t.getPlayerName());
        }
        return ret;
    }

    /**
     * Constructor of server socket handler
     * @param port number to be connected to (integer)
     * @param numberOfPlayers integer defining number of playes allowed
     * @param webClient webclient instance, 
     * @param isServerPlaying
     */
    public ServerSocketHandler(int port, int numberOfPlayers, WebClient webClient, boolean isServerPlaying) {
        this.webClient = webClient;
        NUMBER_OF_PLAYERS = numberOfPlayers;
        PORT = port;
        this.isServerPlaying = isServerPlaying;
        try {
            this.server = new ServerSocket(PORT);
            // Initially, the socket timeout would be 1s.
            // server.setSoTimeout(1000);
        } catch (IOException e) {
            System.err.print("The server could not be started:\n\t");
            System.err.println(e.getMessage());
        }
    }

    /**
     * Main thread loop
     */
    public void run() {
        int i = 0;
        clientSocketPool = new ArrayList<>();
        MessageQueue s = new MessageQueue(isServerPlaying);
        while (!gameInProgress) {
            try {
                // Open the gates!
                Socket temp = server.accept();
                System.out.println("New client connected");
                ListenerThread client = new ListenerThread(temp, i, new NetworkClient(null,null), ACK_TIMEOUT, MOVE_TIMEOUT, s);
                client.initialiseConnection();
                clientSocketPool.add(i++, client);
                // Make new Thread for client.
                Thread t = new Thread(client);
                t.start();
                threads.add(t);

                if (i == NUMBER_OF_PLAYERS) {
                    gameInProgress = true;
                    // From this stage on, any new connection will be rejected.
                    reject = new RejectingThread(server);
                    t = new Thread(reject);
                    t.start();
                }
            } catch (IOException | InitialisationException e) {
                e.printStackTrace();
            }
        }

        try {
            while (notAllInitialised(InitState.STAGE_PING)) ;  //wait for all clients to pass the init stage.
            s.sendPing(i);
            while (notAllInitialised(InitState.STAGE_READY)) ;  //wait on ping commands to be received.
            s.sendReady();
            while (notAllInitialised(InitState.STAGE_PLAYING)) ;  //wait on acknowledgements
            InitialiseGameCommand command = generateInitGame();
            s.sendAll(command, isServerPlaying ? 0 : null);

            // Setting up the initial game state
            State gameState = new State();
            webClient.setState(gameState);
            Map map = new Map();
            gameState.setup(map, ListenerThread.getPlayers());
            HostForwarder.setState(gameState);
            seed = new RandomNumberGenerator();
            HostForwarder.setSeed(seed);

            // Elect first player by dice roll and shuffle cards deck.
            while (notAllInitialised(InitState.FIRST_PLAYER_ELECTABLE));
            seed.finalise();
            getFirstPlayer(gameState);

            Thread.sleep(10);

            HostForwarder.setSeed(seed);
            ListenerThread.shuffleCards();
            while (notAllInitialised(InitState.DECK_SHUFFLED));
            seed.finalise();
            gameState.shuffleRiskCards(seed);
            System.err.println("SHUFFLED CARD DECK");
            HostForwarder.setSeed(null);

        } catch (IOException e){
            System.err.println("Error while initialising game");
        } catch (HashMismatchException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //TODO ListenerThread remove player if error occurs!

        // When all ListenerThreads finished, close the game gracefully.
        try {
            for (Thread t : threads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            reject.stop();
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private synchronized void getFirstPlayer(State gameState) throws HashMismatchException {
        int startingPlayer = (int)(seed.nextInt() % ListenerThread.getPlayers().size());
        HostForwarder.setSeed(null);
        gameState.setFirstPlayer(gameState.getPlayer(startingPlayer));
        gameState.setCurrentPlayer(startingPlayer);
        System.err.println("STARTING PLAYER IS " + startingPlayer);
        this.seed = new RandomNumberGenerator();
    }

    private InitialiseGameCommand generateInitGame() {
        ArrayList<String> customisations = clientSocketPool.get(0).getCustoms();
        int version = 1;
        for(ListenerThread t : clientSocketPool){
            int this_version = t.getVersion();
            ArrayList<String> customs = t.getCustoms();

            if (this_version<version){
                version = this_version;
            }
            customisations.retainAll(customs);
        }
        JSONArray arr = new JSONArray();
        arr.addAll(customisations);
        return new InitialiseGameCommand(version, arr);

    }

    private boolean notAllInitialised(InitState state) {
        for (ListenerThread t : clientSocketPool){
            if (!t.initialised(state)){
                return true;
            }
        }
        System.out.println("All initialised, advancing to next stage");
        return false;
    }

    public int getNUMBER_OF_PLAYERS() {
        return NUMBER_OF_PLAYERS;
    }
}
