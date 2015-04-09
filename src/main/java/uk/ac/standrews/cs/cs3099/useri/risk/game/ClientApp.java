package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.json.simple.JSONArray;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.CLIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.WebClient;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.ClientSocketHandler;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.JoinGameCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Stack;

public class ClientApp {


    public static final int SUCCESS = 0;
    public static final int BAD_ADDRESS = -1;
    public static final int CANNOT_OPEN_STREAMS = -2;
    public static final int JOIN_REJECTED = -3;
    public static final int COMMUNICATION_FAILED = -4;
    public static final int PROTOCOL_ERROR_DETECTED = -5;

    public static int run (String address, int port, Client localClient) {
        ClientSocketHandler socketHandler = new ClientSocketHandler();
        float[] versions = {1};
        String[] features = {};
        String name = "Some guy";

        //connect and obtain game information
        int result = socketHandler.initialise(address,port,localClient,versions,features,name);

        if(result == SUCCESS) {
            //Run the socket handler
            Thread socketHandlerThread = new Thread(socketHandler);
            socketHandlerThread.start();

            //initialise and start the game engine thread
            GameEngine engine = new GameEngine(socketHandler);
            Thread gameEngineThread = new Thread(engine);
            gameEngineThread.start();
        }

        return result;
       
    }
    public static void main(String argv[]){
        //hardcode port and address for now
        int port = 1234;
        String address = "localhost";

        //try to connect to server
        run(address,port, new CLIClient(null));


    }
}
