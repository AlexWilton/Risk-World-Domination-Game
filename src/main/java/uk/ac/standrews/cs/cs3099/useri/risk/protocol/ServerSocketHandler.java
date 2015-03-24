package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.NetworkClient;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Random;

public class ServerSocketHandler {
    private final int PORT, ACK_TIMEOUT, MOVE_TIMEOUT, MAX_PLAYER_COUNT = 3, MIN_PLAYER_COUNT = 3;

    private ServerSocket server;
    private ArrayList<ListenerThread> clientSocketPool;

    private boolean gameInProgress = false;


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
        clientSocketPool = new ArrayList<>();
        MessageQueue s = new MessageQueue(2, playing);
        while (!gameInProgress) {
            try {
                // TODO set up the initial game state.
                Socket temp = server.accept();
                System.out.println("New client connected");
                ListenerThread client = new ListenerThread(temp, i, new NetworkClient(), gameInProgress, ACK_TIMEOUT, MOVE_TIMEOUT, s);
                clientSocketPool.add(i++, client);
                // Make new Thread for client.
                Thread t = new Thread(client);
                t.start();

                // Decide whether we want to start the game already, partially randomly.
                Random r = new Random(System.nanoTime());
                if (i == MAX_PLAYER_COUNT - 1) {

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

            }
        }

        while (!allInitialised());  //wait for all clients to pass the init stage.
        s.sendPing(i);
        while (!allInitialised());  //wait on ping commands to be received.
        s.sendReady();
        while (!allInitialised());  //wait on acknowledgements
        System.out.println("Stuff seems to be working");

        while (true) {
            try {
                Socket temp = server.accept();
                System.out.println("New client connected");
                ListenerThread client = new ListenerThread(temp, i, new NetworkClient(), gameInProgress, ACK_TIMEOUT, MOVE_TIMEOUT, s);
                // Make new Thread for client.
                Thread t = new Thread(client);
                t.start();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }

    private boolean allInitialised() {
        for (ListenerThread t : clientSocketPool){
            if (!t.initialised()){
                return false;
            }
        }
        System.out.println("All initialised, advancing to next stage");
        return true;
    }


}
