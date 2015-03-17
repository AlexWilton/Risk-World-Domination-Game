package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

import org.json.simple.parser.JSONParser;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.*;

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
    private SignalJoinedPlayer stuff;
    private static ArrayList<Player> players = new ArrayList<>();
    private final int ACK_TIMEOUT, MOVE_TIMEOUT;
    protected final Socket sock;
    protected final int ID;
    protected final Client client;
    private final boolean gameInProgress;
    private PrintWriter output;
    private BufferedReader input;
    private JSONParser parser;
    private String playerName;
    private boolean initialised;


    public ListenerThread(Socket sock, int id, Client client, boolean gameInProgress, int ack_timeout, int move_timeout, SignalJoinedPlayer s) {
        this.stuff = s;
        this.sock = sock;
        this.ID = id;
        this.client = client;
        this.gameInProgress = gameInProgress;
        this.ACK_TIMEOUT = ack_timeout;
        this.MOVE_TIMEOUT = move_timeout;

        this.parser = new JSONParser();
    }

    /**
     * Initialises the connection and returns whether it was successful.
     * @throws IOException
     */
    private synchronized boolean initialiseConnection() throws IOException {
        Command command = JoinGame.parse(input.readLine());
        if(command == null) {
            reply(new Acknowledgement(32768, 200, null));
            purgeConnection();
            return false;
        }
        else if (command instanceof JoinGame) {
            reply(new AcceptJoinGame(ACK_TIMEOUT, MOVE_TIMEOUT, ID));
            client.setPlayerId(ID);
            playerName = ((JoinGame) command).getName();
            players.add(new Player(ID, client, playerName));
            if (playerName != null) {
                // Send player list to all connected players.
                stuff.send(players);
                reply(stuff.signal(players.size()));
            }
            //TODO Now that the player is added, what happens?
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
        output.println(command);
        output.flush();
    }

    private void rejectGame() throws IOException {
        if (JoinGame.parse(input.readLine()) == null)
            reply(new Acknowledgement(32768, 200, null));
        else
            reply(new RejectJoinGame("Game already in progress"));

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
                initialised = initialiseConnection();
            }
            //wait for other threads to join game.
            while(true) {
                reply(stuff.signal(players.size()));
            }


        } catch(IOException e){
            //TODO don't leave the miserable exceptions alone... :(
        }

    }

    public boolean initialised() {
        return initialised;
    }
}
