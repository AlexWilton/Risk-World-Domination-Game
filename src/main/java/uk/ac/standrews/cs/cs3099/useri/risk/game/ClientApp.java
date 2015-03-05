package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.JoinGame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by po26 on 05/03/15.
 */
public class ClientApp {


    public static final int SUCCESS = 0;
    public static final int BAD_ADDRESS = -1;

    public static int run (String address, int port) {
        //make join command
        JSONArray versions = new JSONArray();
        JSONArray features = new JSONArray();
        versions.add(0.1f);
        JoinGame joinCommand = new JoinGame(versions,features);
        String joinCommandJSON = joinCommand.toJSONString();

        //make the socket
        Socket clientSocket = null;
        try {
            clientSocket = new Socket(address,port);
        }
        catch (IOException e){
            System.out.println("Cant connect to server");
            return BAD_ADDRESS;
        }

        //make writers and readers

        try{
            PrintWriter out =
                    new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));
        }
        catch (IOException e){
            System.out.println("wrong");
        }

        return SUCCESS;
    }
    public static void main(String argv[]){
        //hardcode port and address for now
        int port = 1234;
        String address = "localhost";

        //try to connect to server
        run(address,port);


    }
}