package uk.ac.standrews.cs.cs3099.useri.risk.helpers;


import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import uk.ac.standrews.cs.cs3099.useri.risk.action.DeployArmyAction;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.NetworkClient;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;


import java.io.*;
import java.net.Socket;
import java.util.ArrayList;


/**
 * Created by po26 on 12/02/15.
 */
public class ClientSocketDistributor implements Runnable{


    private ArrayList<NetworkClient> clients;

    private Socket clientSocket;

    private State gameState;


    public ClientSocketDistributor(Socket clientSocket, ArrayList<NetworkClient> clients){
        this.clients = clients;
        this.clientSocket = clientSocket;
    }

    public ClientSocketDistributor(String host, int port, ArrayList<NetworkClient> clients){
        this(null, clients);

        try{
            clientSocket = new Socket(host, port);


        }
        catch (IOException e){
            System.out.println("Cant connect to server");
            System.exit(-1);
        }

    }

    public void run (){

        try{
            PrintWriter out =
                    new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));
            while(true){
                String currentIn = "";
                while (StringUtils.countMatches(currentIn,"{") != StringUtils.countMatches(currentIn,"}") || StringUtils.countMatches(currentIn,"{") == 0){
                    currentIn += in.readLine();
                }
                interpretJSONMessage(currentIn);
            }
        }
        catch (IOException e){
            System.out.println("wrong");
        }

    }

    private void interpretJSONMessage(String message){
        JSONObject messageObject;
        messageObject = (JSONObject) JSONValue.parse(message);

        String command = messageObject.get("command").toString();
        int player = Integer.parseInt(messageObject.get(("player_id")).toString());

        //handle different commands

        if (command.equals(Commands.TRADE_COMMAND)){
            interpretTradeCommand(messageObject, player);
        }
        else if (command.equals(Commands.DEPLOY_COMMAND)){
            interpretDeployCommand(messageObject,player);
        }
        else {
            System.out.println("NOT IMPLEMENTED COMMAND: " + command);
            System.out.println(message);
        }
    }

    private void interpretTradeCommand(JSONObject commandObject, int player){
        System.out.println("trade action by player " + player);
    }
    private void interpretDeployCommand(JSONObject commandObject, int player) {

        JSONArray territoryNumberPairs = (JSONArray)(commandObject.get("payload"));
        //construct amount of deploy actions
        for (Object pair : territoryNumberPairs) {

            //extract parameters
            JSONArray pairArray = (JSONArray) pair;
            int countryId = Integer.parseInt(pairArray.get(0).toString());
            int armies = Integer.parseInt(pairArray.get(1).toString());

            //create deploy action
            //DeployArmyAction ac = new DeployArmyAction(gameState.getPlayers().get(player),gameState.getCountryByID(countryId),armies);

            //push to client

            //clients.get(player).pushAction(ac);

            //pushed action to client

            System.out.println("pushed deploy action to client " + player);

            //test command
            /*{
                "command": "deploy",
                    "payload": [[1,1],[2,1]],
                "ack_id": 1,
                    "player_id" : 1

            }*/


        }

    }

}
