package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

import uk.ac.standrews.cs.cs3099.useri.risk.action.Action;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.NetworkClient;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * Created by bs44 on 19/02/15.
 */
public class ServerSocketHandler {
    private final int PORT, ACK_TIMEOUT, MOVE_TIMEOUT;

    private ServerSocket server;
    private ArrayList<ListenerThread> clientSocketPool;
    private ArrayList<Action> actionPool;

    private byte[] buffer;
    private boolean gameInProgress;


    public ServerSocketHandler(int port, int ack_timeout, int move_timeout) {
        this.PORT = port;
        this.ACK_TIMEOUT = ack_timeout;
        this.MOVE_TIMEOUT = move_timeout;
        try {
            this.server = new ServerSocket(PORT);
            // Initially, the socket timeout would be 1s.
            server.setSoTimeout(1000);
        } catch (IOException e) {
            System.err.print("The server could not be started:\n\t");
            System.err.println(e.getMessage());
        }
    }

    public void startServer(boolean playing) {
        int i = playing? 1 : 0;         //If the server is playing, first client gets ID 1, otherwise 0.
        clientSocketPool = new ArrayList<ListenerThread>();
        while (true) {
            try {
                Socket temp = server.accept();
                System.out.println("New client connected");
                ListenerThread client = new ListenerThread(temp, i++, new NetworkClient(), gameInProgress, ACK_TIMEOUT, MOVE_TIMEOUT);
                clientSocketPool.add(client);
                // Make new Thread for client.
                client.run();

                if (i>=1){
                    gameInProgress = true;
                }

            } catch (SocketTimeoutException timeout) {
                // No more clients wanted to connect, so just carry on working.
                gameInProgress = true;
                //break;
            } catch (IOException e) {

            }
        }
    }


}
