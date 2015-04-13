package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
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
public class ListenerThread implements Runnable {
    private static ArrayList<Player> players = new ArrayList<>();
    //private static State gameState;

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
            messageQueue.addPlayer(ID, output);
            //System.out.println("Player " + ID +": " + output);
            client.setPlayerId(ID);
            String playerName = ((JoinGameCommand) command).getName();
            player = new Player(ID, client, playerName);
            players.add(player);

            customs = ((JoinGameCommand) command).getFeatures();
            version = ((JoinGameCommand) command).getVersion();
            // Send player list to all connected players.
            messageQueue.sendPlayerList(players);
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

    private void reply(Command command) {
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
                Command comm;
                /*while ((comm = messageQueue.probablyGetMessage(ID)) != null){
                    reply(comm);
                }*/
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

            // here, the game is initialised with a final list of players.
            /*while (true){
                //Command comm = messageQueue.getMessage(ID);
                /*reply(comm);
                if (comm == null) continue;
                if (comm.getClass().equals(InitialiseGameCommand.class)){
                    break;
                }/
            }*/

            // Start forwarding every message as is.
            HostForwarder fw = new HostForwarder(messageQueue, MOVE_TIMEOUT, ACK_TIMEOUT, ID, input, output);
            fw.playGame(null);

        } catch(InitialisationException | SocketTimeoutException f) {
            Command comm;
            /*while ((comm = messageQueue.probablyGetMessage(ID)) != null) {
                reply(comm);
            }*/
            messageQueue.sendAll(new TimeOutCommand(ID, null), null);
            //reply(messageQueue.getMessage(ID));
            purgeConnection();
        } catch(IOException e) {
            e.printStackTrace();
        }

        purgeConnection();
    }



    private Command waitingOn(Class<?> c){
        Command reply;
        while(true){
            //Command comm = messageQueue.probablyGetMessage(ID);
            //reply(comm);
            //if (comm == null)
                //continue;
            //if (comm.getClass().equals(c)) {
                try {
                    while (!input.ready());
                    reply = Command.parseCommand(input.readLine());
                    //while ((comm = messageQueue.probablyGetMessage(ID)) != null)
                     //   reply(comm);
                    messageQueue.sendAll(reply, ID);
                    break;
                } catch (IOException e){
                    e.printStackTrace();
                }

            }
        //}
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

    public static void setState(State state) {
        //gameState = state;
    }
}