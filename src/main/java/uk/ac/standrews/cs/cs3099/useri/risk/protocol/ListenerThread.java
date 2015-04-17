package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.HashMismatchException;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.*;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.exceptions.InitialisationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * Wrapper class for each client socket. This class is used by the server to keep track of connections to clients.
 * All fields are final and protected.
 */
class ListenerThread implements Runnable {
    private static ArrayList<Player> players = new ArrayList<>();
    private static boolean shuffle;

    private final int ACK_TIMEOUT, MOVE_TIMEOUT;
    private final Socket sock;
    private final int ID;

    private Client client;
    private Player player;
    private PrintWriter output;
    private BufferedReader input;
    private InitState state = InitState.STAGE_CONNECTING;
    private MessageQueue messageQueue;
    private int version;
    private ArrayList<String> customs;
    private HostForwarder fw;


    public ListenerThread(Socket sock, int id, Client client, int ack_timeout, int move_timeout, MessageQueue q) {
        ACK_TIMEOUT = ack_timeout * 1000;
        MOVE_TIMEOUT = move_timeout * 1000;
        ID = id;
        this.sock = sock;
        this.client = client;
        this.messageQueue = q;
    }

    /**
     * Initialises the connection and returns whether it was successful.
     * @throws IOException
     */
    private synchronized boolean initialiseConnection() throws IOException, InitialisationException{
        Command command = Command.parseCommand(input.readLine());
        if (command instanceof JoinGameCommand) {
            reply(new AcceptJoinGameCommand(ACK_TIMEOUT, MOVE_TIMEOUT, ID));

            String playerName = ((JoinGameCommand) command).getName();
            player = new Player(ID, client, playerName);
            players.add(player);
            customs = ((JoinGameCommand) command).getFeatures();
            version = ((JoinGameCommand) command).getVersion();
            // Send player list to all connected players.
            messageQueue.addPlayer(ID, this, player);
            reply(new PlayersJoinedCommand(players));
            //reply(messageQueue.getMessage(ID));
            return true;
        } else {
            throw new InitialisationException("Unrecognised command received");
        }
    }

    private void purgeConnection(){
        try {
            output.flush();
            output.close();
            input.close();
            sock.close();
            players.remove(player);
        } catch (IOException e) {
            System.err.println("Error occurred while closing connection, this is not fatal.");
        }
    }

    void reply(Command command) {
        if (command == null)
            return;
        System.out.println("Player " + ID + ": " + command);
        output.println(command);
        output.flush();
    }

    /**
     * non-javadoc
     * This implements runnable. In fact, it will only handle exceptions basically.
     */
    @Override
    public void run() {
        try {
            // Set up the socket i/o and the default timeout
            sock.setSoTimeout(MOVE_TIMEOUT);
            input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            output = new PrintWriter(sock.getOutputStream());

            // Initialise the connection
            if(initialiseConnection()){
                state = state.next();
            }

            // Send ping and receive pong.
            Command reply = waitingOn(PingCommand.class);
            if (!(reply instanceof PingCommand)){
                throw new InitialisationException("Ping command was not answered.");
            }
            state = state.next();

            // Send ready and receive ack.
            sock.setSoTimeout(ACK_TIMEOUT);
            reply = waitingOn(ReadyCommand.class);
            sock.setSoTimeout(MOVE_TIMEOUT);
            if (!(reply instanceof AcknowledgementCommand) /*|| ((AcknowledgementCommand)reply).getAcknowledgementId() != Command.getLastAckID()*/){
                throw new InitialisationException("Ready command was not acknowledged in time.");
            }
            state = state.next();



            // Start forwarding every message as is.
            fw = new HostForwarder(messageQueue, MOVE_TIMEOUT, ACK_TIMEOUT, ID, input);
            while (!fw.hasSeed()) Thread.sleep(10);
            fw.getRolls();
            state = InitState.FIRST_PLAYER_ELECTABLE;
            //HostForwarder.setSeed(null);
            while (!(fw.hasSeed() && shuffle)) Thread.sleep(10);
            fw.getRolls();
            state = InitState.DECK_SHUFFLED;
            fw.playGame();

        } catch(InitialisationException | SocketTimeoutException f) {
            try {
                messageQueue.sendAll(new TimeOutCommand(ID), null);
                purgeConnection();
            } catch (Exception e) {
                System.err.println("Error when sending timeout");
            }
        } catch(IOException | InterruptedException e) {
            e.printStackTrace();
        } catch (HashMismatchException e) {
            e.printStackTrace();
        }

        purgeConnection();
    }



    private Command waitingOn(Class<?> c){
        Command reply;
        while(true){
                try {
                    while (!input.ready());
                    reply = Command.parseCommand(input.readLine());
                    messageQueue.sendAll(reply, ID);
                    break;
                } catch (IOException e){
                    e.printStackTrace();
                }

            }
        return reply;
    }

    public boolean initialised(InitState instate) {
        return state.equals(instate);
    }

    public int getVersion() {
        return version;
    }

    public ArrayList<String> getCustoms() {
        return customs;
    }

    public static ArrayList<Player> getPlayers() {
        return players;
    }

    public void signalAck(int ack_id) throws IOException{
        if (fw != null)
            fw.signalAck(ack_id);
    }

    public static void shuffleCards() {
        shuffle = true;
    }

    public void getRollsLater() throws IOException, InterruptedException {
        fw.getRollsLater();
    }
}