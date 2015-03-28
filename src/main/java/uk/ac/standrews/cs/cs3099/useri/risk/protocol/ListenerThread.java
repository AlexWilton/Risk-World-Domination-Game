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
import java.util.ArrayList;

/**
 * Wrapper class for each client socket. This class is used by the server to keep track of connections to clients.
 * All fields are final and protected.
 */
public class ListenerThread implements Runnable {
    private MessageQueue stuff;
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


    public ListenerThread(Socket sock, int id, Client client, boolean gameInProgress, int ack_timeout, int move_timeout, MessageQueue s) {
        this.stuff = s;
        this.sock = sock;
        this.ID = id;
        this.client = client;
        this.gameInProgress = gameInProgress;
        this.ACK_TIMEOUT = ack_timeout;
        this.MOVE_TIMEOUT = move_timeout;
    }

    /**
     * Initialises the connection and returns whether it was successful.
     * @throws IOException
     */
    private synchronized boolean initialiseConnection() throws IOException {
        Command command = JoinGameCommand.parse(input.readLine());
        if(command == null) {
            reply(new AcknowledgementCommand(32768, 200, null));
            purgeConnection();
            return false;
        }
        else if (command instanceof JoinGameCommand) {
            reply(new AcceptJoinGameCommand(ACK_TIMEOUT, MOVE_TIMEOUT, ID));
            stuff.addPlayer(ID);
            client.setPlayerId(ID);
            String playerName = ((JoinGameCommand) command).getName();
            players.add(new Player(ID, client, playerName));
            customs = ((JoinGameCommand) command).getFeatures();
            version = ((JoinGameCommand) command).getVersion();
            if (playerName != null) {
                // Send player list to all connected players.
                stuff.sendPlayerList(players);
                reply(stuff.getMessage(ID));
            }
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
        output.println(command);
        output.flush();
    }

    private void rejectGame() throws IOException {
        if (JoinGameCommand.parse(input.readLine()) == null)
            reply(new AcknowledgementCommand(32768, 200, null));
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
            input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            output = new PrintWriter(sock.getOutputStream());

            if (gameInProgress) {
                rejectGame();
            } else {
                if(initialiseConnection()) state = state.next();
            }

            Command reply = waitingOn(PingCommand.class);
            if (!(reply instanceof PingCommand)){
                System.out.println("Error, no ping command received");
                //error here
            }
            System.out.println("Ping reply received: "+ ID);
            state = state.next();     // Ping reply received

            reply = waitingOn(ReadyCommand.class);

            if (!(reply instanceof AcknowledgementCommand) || ((AcknowledgementCommand)reply).getAcknowledgementId() != 1){
                throw new InitialisationException("Acknowledgement error");
            }
            state = state.next();    // Ready acknowledgement received

            // here, the game is initialised with a final list of players.
            while (true){
                Command comm = stuff.getMessage(ID);
                reply(comm);
                if (comm == null) continue;
                if (comm.getClass().equals(InitialiseGameCommand.class)){
                    break;
                }
            }


        } catch(IOException e){
            e.printStackTrace();
        } catch(InitialisationException f) {
            //TODO send error message and remove this player.
        }

    }

    private Command waitingOn(Class<?> c){
        Command reply;
        while(true){
            Command comm = stuff.getMessage(ID);
            reply(comm);
            if (comm == null)
                continue;
            if (comm.getClass().equals(c)) {
                try {
                    reply = Command.parseCommand(input.readLine());
                    reply(stuff.probablygetMessage(ID));
                    stuff.sendAll(reply);
                    System.out.println("Stuff " + ID);
                    break;
                } catch (IOException e){
                    e.printStackTrace();
                }

            }
        }
        System.out.println("reply");
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
}
