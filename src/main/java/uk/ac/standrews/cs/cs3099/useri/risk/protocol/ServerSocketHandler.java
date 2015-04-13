package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

import org.json.simple.JSONArray;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.NetworkClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.WebClient;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Map;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.InitialiseGameCommand;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class ServerSocketHandler implements Runnable {
    private final int PORT, NUMBER_OF_PLAYERS, ACK_TIMEOUT = 1, MOVE_TIMEOUT = 30;
    public static final int MAX_PLAYER_COUNT = 6, MIN_PLAYER_COUNT = 2; //needed for web client to know the range of allowed number of players. (needs to be public)
    private WebClient webClient;
    private ServerSocket server;
    private ArrayList<ListenerThread> clientSocketPool;
    private boolean isServerPlaying;

    private boolean gameInProgress = false;
    private ArrayList<Player> players = new ArrayList<>();

    public ServerSocketHandler(int port, int numberOfPlayers, WebClient webClient, boolean isServerPlaying) {
        this.webClient = webClient;
        NUMBER_OF_PLAYERS = numberOfPlayers;
        PORT = port;
        this.isServerPlaying = isServerPlaying;
        try {
            this.server = new ServerSocket(PORT);
            // Initially, the socket timeout would be 1s.
            server.setSoTimeout(1000);
        } catch (IOException e) {
            System.err.print("The server could not be started:\n\t");
            System.err.println(e.getMessage());
        }
    }

    public void run() {
        int i = 0;// isServerPlaying ? 1 : 0;         //If the server is isServerPlaying, first client gets ID 1, otherwise 0.
        clientSocketPool = new ArrayList<>();
        MessageQueue s = new MessageQueue(NUMBER_OF_PLAYERS);

        while (!gameInProgress) {
            try {
                // Open the gates!
                Socket temp = server.accept();
                System.out.println("New client connected");
                //TODO GAMESTATE!!
                ListenerThread client = new ListenerThread(temp, i, new NetworkClient(null), gameInProgress, ACK_TIMEOUT, MOVE_TIMEOUT, s);
                clientSocketPool.add(i++, client);
                // Make new Thread for client.
                Thread t = new Thread(client);
                t.start();

                // Decide whether we want to start the game already, partially randomly.
                //Random r = new Random(System.nanoTime());
                if (i == NUMBER_OF_PLAYERS) {

                    gameInProgress = true;
                }
                //if (i>MIN_PLAYER_COUNT && r.nextDouble()>=0.5)
                    //gameInProgress = true;

            } catch (SocketTimeoutException timeout) {
                // No more clients wanted to connect, so just carry on working.
                // TODO Probably not final here.
                //gameInProgress = true;
                //break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        while (!allInitialised(InitState.STAGE_PING));  //wait for all clients to pass the init stage.
        s.sendPing(i, isServerPlaying?0:null);
        while (!allInitialised(InitState.STAGE_READY));  //wait on ping commands to be received.
        s.sendReady(isServerPlaying?0:null);
        while (!allInitialised(InitState.STAGE_PLAYING));  //wait on acknowledgements
        InitialiseGameCommand command = generateInitGame();
        //System.out.println(command);
        s.sendAll(command, isServerPlaying?0:null);
        //System.out.println("Stuff seems to be working");

        //TODO ListenerThread remove player if error occurs!
        State st = new State(new Map(), ListenerThread.getPlayers());
        webClient.setState(st);

        while (true) {
            try {
                Socket temp = server.accept();
                System.out.println("New client connected");
                //TODO GAME STATE!!
                ListenerThread client = new ListenerThread(temp, i, new NetworkClient(null), gameInProgress, ACK_TIMEOUT, MOVE_TIMEOUT, s);
                // Make new Thread for client.
                Thread t = new Thread(client);
                t.start();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
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

    private boolean allInitialised(InitState state) {
        for (ListenerThread t : clientSocketPool){
            if (!t.initialised(state)){
                return false;
            }
        }
        System.out.println("All initialised, advancing to next stage");
        return true;
    }


}
