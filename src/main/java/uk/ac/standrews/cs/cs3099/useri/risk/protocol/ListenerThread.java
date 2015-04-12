package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
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
    private MessageQueue messageQueue;
    private static ArrayList<Player> players = new ArrayList<>();
    private final int ACK_TIMEOUT, MOVE_TIMEOUT;
    protected final Socket sock;
    protected final int ID;
    protected final Client client;
    private final boolean gameInProgress;
    private PrintWriter output;
    private BufferedReader input;
    private InitState state = InitState.STAGE_CONNECTING;
    private int version;
    private ArrayList<String> customs;


    public ListenerThread(Socket sock, int id, Client client, boolean gameInProgress, int ack_timeout, int move_timeout, MessageQueue q) {
        this.messageQueue = q;
        this.sock = sock;
        this.ID = id;
        this.client = client;
        this.gameInProgress = gameInProgress;
        this.ACK_TIMEOUT = ack_timeout * 1000;
        this.MOVE_TIMEOUT = move_timeout * 1000;

    }

    /**
     * Initialises the connection and returns whether it was successful.
     * @throws IOException
     */
    private synchronized boolean initialiseConnection() throws IOException, InitialisationException{
        Command command = Command.parseCommand(input.readLine());
        if(command == null) {
            //reply(new AcknowledgementCommand(ID));//TODO added hardcoded player id to make it work
            throw new InitialisationException("Unparseable command received");
        }
        else if (command instanceof JoinGameCommand) {
            reply(new AcceptJoinGameCommand(ACK_TIMEOUT, MOVE_TIMEOUT, ID));
            messageQueue.addPlayer(ID);
            client.setPlayerId(ID);
            String playerName = ((JoinGameCommand) command).getName();
            players.add(new Player(ID, client, playerName));
            customs = ((JoinGameCommand) command).getFeatures();
            version = ((JoinGameCommand) command).getVersion();
            // Send player list to all connected players.
            messageQueue.sendPlayerList(players);
            reply(messageQueue.getMessage(ID));
            return true;
        }
        return false;
    }

    private void purgeConnection() throws IOException {
        output.flush();
        output.close();
        input.close();
        sock.close();
    }

    private void reply(Command command) {
        if (command == null)
            return;
        System.out.println("Player " + ID + ": " + command);
        output.println(command);
        output.flush();
    }

    private void rejectGame() throws IOException, InitialisationException {
        if (JoinGameCommand.parse(input.readLine()) == null)
            throw new InitialisationException("Unparseable command received");
        else
            reply(new RejectJoinGameCommand("Game already in progress"));

        purgeConnection();
    }



    /**
     * non-javadoc
     * This implements runnable. In fact, it will only handle exceptions basically.
     */
    @Override
    public void run() {
        try {
            sock.setSoTimeout(MOVE_TIMEOUT);
            input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            output = new PrintWriter(sock.getOutputStream());

            if (gameInProgress) {
                rejectGame();
            } else {
                if(initialiseConnection()) state = state.next();
            }

            Command reply = waitingOn(PingCommand.class);
            if (!(reply instanceof PingCommand)){
                throw new InitialisationException("Ping command was not answered.");
            }
            //System.out.println("Ping reply received: "+ ID);
            state = state.next();     // Ping reply received

            sock.setSoTimeout(ACK_TIMEOUT);
            reply = waitingOn(ReadyCommand.class);
            sock.setSoTimeout(MOVE_TIMEOUT);

            if (!(reply instanceof AcknowledgementCommand) /*|| ((AcknowledgementCommand)reply).getAcknowledgementId() != Command.getLastAckID()*/){
                throw new InitialisationException("Ready command was not acknowledged in time.");
            }
            state = state.next();    // Ready acknowledgement received

            // here, the game is initialised with a final list of players.
            while (true){
                Command comm = messageQueue.getMessage(ID);
                reply(comm);
                if (comm == null) continue;
                if (comm.getClass().equals(InitialiseGameCommand.class)){
                    break;
                }
            }

            // Start forwarding every message as is.
            double timer = System.currentTimeMillis();
            double diff = MOVE_TIMEOUT;
            boolean ack_required = false;
            int lastack = 0;
            while(true) {
                if (ack_required && System.currentTimeMillis() > timer + diff) {
                    throw new SocketTimeoutException();
                }
                Command comm;
                while ((comm = messageQueue.probablygetMessage(ID)) != null) {
                    reply(comm);
                    if (comm.requiresAcknowledgement()){
                        timer = System.currentTimeMillis();
                        ack_required = true;
                        diff = ACK_TIMEOUT;
                        lastack = Integer.parseInt(comm.get("ack_id").toString());

                    } else if (comm instanceof AcknowledgementCommand) {
                        System.out.println(ID + ": Last Ack ID: " + lastack);
                        System.out.println(ID + ": Recv Ack ID: " + ((AcknowledgementCommand) comm).getAcknowledgementId());
                        if (((AcknowledgementCommand) comm).getAcknowledgementId() == lastack) {
                            ack_required = false;
                            timer = System.currentTimeMillis();
                            diff = MOVE_TIMEOUT;
                        }
                    } else if (!ack_required) {
                        timer = System.currentTimeMillis();
                    } else {
                        diff = MOVE_TIMEOUT;
                    }
                }
                if (input.ready()) {
                    reply = Command.parseCommand(input.readLine());
                    messageQueue.sendAll(reply, ID);
                    //System.out.println("Player " + ID + " received " + reply);
                    if (reply.requiresAcknowledgement()) {
                        timer = System.currentTimeMillis();
                        ack_required = true;
                        diff = ACK_TIMEOUT;
                        lastack = Integer.parseInt(reply.get("ack_id").toString());
                    } else if (reply instanceof AcknowledgementCommand) {
                        System.out.println(ID + ": Last Ack ID: " + lastack);
                        System.out.println(ID + ": Recv Ack ID: " + ((AcknowledgementCommand) reply).getAcknowledgementId());
                        if (((AcknowledgementCommand) reply).getAcknowledgementId() == lastack) {
                            ack_required = false;
                            timer = System.currentTimeMillis();
                            diff = MOVE_TIMEOUT;
                        }
                    } else if (!ack_required){
                        timer = System.currentTimeMillis();
                    } else {
                        diff = MOVE_TIMEOUT;
                    }
                }
            }
        } catch(InitialisationException | SocketTimeoutException f) {
            messageQueue.sendAll(new TimeOutCommand(ID, null), null);
            reply(messageQueue.getMessage(ID));
        } catch(IOException e) {
            e.printStackTrace();
        }

    }

    private Command waitingOn(Class<?> c){
        Command reply;
        while(true){
            Command comm = messageQueue.getMessage(ID);
            reply(comm);
            if (comm == null)
                continue;
            if (comm.getClass().equals(c)) {
                try {
                    while (!input.ready());
                    reply = Command.parseCommand(input.readLine());
                    reply(messageQueue.probablygetMessage(ID));
                    messageQueue.sendAll(reply, ID);
                    break;
                } catch (IOException e){
                    e.printStackTrace();
                }

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
}