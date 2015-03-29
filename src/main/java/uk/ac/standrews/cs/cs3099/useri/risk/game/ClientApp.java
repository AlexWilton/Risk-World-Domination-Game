package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.json.simple.JSONArray;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.WebClient;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.ClientSocketHandler;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.JoinGameCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientApp {


    public static final int SUCCESS = 0;
    public static final int BAD_ADDRESS = -1;
    public static final int CANNOT_OPEN_STREAMS = -2;
    public static final int JOIN_REJECTED = -3;
    public static final int COMMUNICATION_FAILED = -4;

    public static int run (String address, int port, WebClient webClient) {
        ClientSocketHandler socketHandler = new ClientSocketHandler();
        float[] versions = {0.1f};
        String[] features = {};
        socketHandler.initialise(address,port,webClient,versions,features);



        return SUCCESS;
    }
    public static void main(String argv[]){
        //hardcode port and address for now
        int port = 1234;
        String address = "localhost";

        //try to connect to server
        run(address,port, new WebClient());


    }
}
