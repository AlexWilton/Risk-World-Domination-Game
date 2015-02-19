package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

import org.json.simple.parser.JSONParser;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Wrapper class for each client socket. This class is used by the server to keep track of connections to clients.
 * All fields are final and protected.
 * Created by bs44 on 19/02/15.
 */
public class ListenerThread implements Runnable {
    private final int ACK_TIMEOUT, MOVE_TIMEOUT;
    protected final Socket sock;
    protected final int ID;
    protected final Client client;
    private final boolean gameInProgress;
    private PrintWriter output;
    private BufferedReader input;
    private JSONParser parser;


    public ListenerThread(Socket sock, int id, Client client, boolean gameInProgress, int ack_timeout, int move_timeout) {
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
    private boolean initialiseConnection() throws IOException {
        if(JoinGame.parse(input.readLine()) == null){
            reply(new Acknowledgement(32768, 200, null));
            purgeConnection();
            return false;
        }
        reply(new AcceptJoinGame(ACK_TIMEOUT, MOVE_TIMEOUT, ID));
        return true;
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
                initialiseConnection();
            }
        } catch(IOException e){
            //TODO don't leave the miserable exceptions alone... :(
        }

    }


}
