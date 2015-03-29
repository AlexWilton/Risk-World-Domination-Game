package uk.ac.standrews.cs.cs3099.useri.risk.helpers;


import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.ArrayQueue;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import uk.ac.standrews.cs.cs3099.useri.risk.action.*;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.NetworkClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.WebClient;
import uk.ac.standrews.cs.cs3099.useri.risk.game.ClientApp;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.RiskCard;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.*;


import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;


/**
 * Created by po26 on 12/02/15.
 */
public class ClientSocketHandler implements Runnable{


    private ArrayList<NetworkClient> remoteClients;

    private Client localClient;

    private Socket clientSocket;

    private State gameState;

    private AttackActionBuilder builder;

    private PrintWriter out;

    private BufferedReader in;

    private Queue<Command> commandQueue;

    private int hostId;



    public ClientSocketHandler() {
        this.remoteClients = new ArrayList<>();
        commandQueue = new ArrayQueue<>();
    }

    public ArrayList<Client> getAllClients(){
        ArrayList<Client> ret = new ArrayList<>();

        ret.add(localClient);
        ret.addAll(remoteClients);

        return ret;
    }

    public Client getClientById(int id){
        for (Client c : remoteClients){
            if (c.getPlayerId() == id){
                return c;
            }
        }
        if (localClient.getPlayerId() == id){
            return localClient;
        }

        return null;
    }


    public int initialise(String address, int port, Client localClient, float[] versions, String[] features){
        //try to connect

        try{
            clientSocket = new Socket(address, port);
            //make the writers/Readers
            out =
                    new PrintWriter(clientSocket.getOutputStream(), true);
            in =
                    new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));

            System.out.println("Connected");

        }
        catch (IOException e){
            e.printStackTrace();
            System.out.println("Cant connect to server");
            return ClientApp.BAD_ADDRESS;
        }

        try{
            //send join message
            JSONArray versionsJSON = new JSONArray();
            for (float version : versions){
                versionsJSON.add(version);
            }
            JSONArray featuresJSON = new JSONArray();
            for (String feature : features){
                featuresJSON.add(feature);
            }
            JoinGameCommand joinCommand = new JoinGameCommand(versionsJSON,featuresJSON);
            System.out.println(joinCommand.toJSONString());
            sendCommand(joinCommand);


            //wait for accept or join
            //run as long as connection is up
            boolean replied = false;
            while(!replied){
                Command reply = getNextCommand();
                if (reply instanceof AcceptJoinGameCommand){
                    //Fill in details for starting the game
                    //make the local client+player
                    String playerIdString = ((JSONObject)reply.get("payload")).get("player_id").toString();
                    localClient.setPlayerId(Integer.parseInt(playerIdString));
                    replied = true;
                    System.out.println("Joined Game!");
                }
                else if ( reply instanceof RejectJoinGameCommand){
                    //was rejected
                    System.out.println("Was rejected!");
                    return ClientApp.JOIN_REJECTED;
                }
            }



            //waiting for hosts ping
            replied = false;
            int amountPlayers = 0;

            while (!replied){
                Command reply = getNextCommand();
                if (reply instanceof PingCommand){
                    String amountPlayersString = reply.get("payload").toString();
                    amountPlayers = Integer.parseInt(amountPlayersString);
                    hostId = Integer.parseInt(reply.get("player_id").toString());
                    System.out.println("Host Ping recieved from " + hostId + " ! Game has " + amountPlayers + " Players.");
                    //add host network client
                    NetworkClient hostCLient = new NetworkClient();
                    hostCLient.setPlayerId(hostId);
                    remoteClients.add(hostCLient);
                    replied = true;
                }
            }

            //send own ping
            sendCommand(new PingCommand(localClient.getPlayerId(),amountPlayers));

            //wait for other pings until server sends ready
            replied = false;
            int ackId = 0;
            while (!replied){
                Command reply = getNextCommand();
                if (reply instanceof PingCommand){
                    int playerId = Integer.parseInt(reply.get("player_id").toString());
                    NetworkClient c = new NetworkClient();
                    c.setPlayerId(playerId);
                    remoteClients.add(c);
                    System.out.println("added new player with id " + playerId);


                }
                else if ( reply instanceof ReadyCommand){
                    replied = true;
                    ackId = Integer.parseInt(reply.get("ack_id").toString());
                    //mark host ready;
                    ((NetworkClient)getClientById(hostId)).setReady(true);

                    System.out.println("host is ready! waiting for acknowledgements by all remote players and sending own acknowledgement");

                }
            }

            //send own acknowledgement

            sendCommand(new AcknowledgementCommand(ackId,0,null,localClient.getPlayerId()));

            //wait for other acknowledgements

            while (!allRemoteClientsReady()){
                Command reply = getNextCommand();
                if (reply instanceof AcknowledgementCommand){
                    JSONObject payload = (JSONObject) reply.get("payload");
                    int recdAccId = Integer.parseInt(payload.get("ack_id").toString());
                    System.out.println(reply.get("player_id").toString());
                    int playerId = Integer.parseInt(reply.get("player_id").toString());
                    if (ackId == recdAccId){
                        ((NetworkClient)getClientById(playerId)).setReady(true);
                        System.out.println("player " + playerId + " ready!");
                    }
                }
            }

            System.out.println("all players ready, negotiation ends!");



        }
        catch (IOException e){
            e.printStackTrace();
            System.out.println("wrong");

            return ClientApp.COMMUNICATION_FAILED;
        }

        return ClientApp.SUCCESS;
    }

    public boolean allRemoteClientsReady(){

        for (NetworkClient c : remoteClients){
            if (!c.isReady())
                return false;
        }

        return true;
    }

    public void run (){
        float[] versions = null;
        String[] features = null;
        try{
            //send join message
            JSONArray versionsJSON = new JSONArray();
            for (float version : versions){
                versionsJSON.add(version);
            }
            JSONArray featuresJSON = new JSONArray();
            for (String feature : features){
                featuresJSON.add(feature);
            }
            JoinGameCommand joinCommand = new JoinGameCommand(versionsJSON,featuresJSON);

            sendCommand(joinCommand);


            //wait for accept or join
            //run as long as connection is up
            while(true){
                interpretJSONMessage(getNextCommand());
            }
        }
        catch (IOException e){
            e.printStackTrace();
            System.out.println("wrong");
        }

    }

    public void sendCommand (Command command){
        out.println(command.toJSONString());
        out.flush();

    }
    public Command getNextCommand () throws IOException{
        String currentIn = "";
        while (StringUtils.countMatches(currentIn,"{") != StringUtils.countMatches(currentIn,"}") || StringUtils.countMatches(currentIn,"{") == 0){
            currentIn += in.readLine();
        }
        Command command = Command.parseCommand(currentIn);
        return command;
    }

    private Command interpretJSONMessage(JSONObject messageObject){

        String command = messageObject.get("command").toString();
        int player = Integer.parseInt(messageObject.get(("player_id")).toString());

        //handle different commands

        if (command.equals(Commands.TRADE_COMMAND)){
            interpretTradeCommand(messageObject, player);
        }
        else if (command.equals(Commands.DEPLOY_COMMAND)){
            interpretDeployCommand(messageObject, player);
        }
        else if (command.equals(Commands.ATTACK_COMMAND)){
            interpretAttackCommand(messageObject, player);
        }
        else if (command.equals(Commands.PLAY_CARDS_COMMAND)){
            interpretPlayCardsCommand(messageObject, player);
        }
        else if (command.equals(Commands.DRAW_CARD_COMMAND)){
            interpretDrawCardCommand(messageObject, player);
        }
        else if (command.equals(Commands.DEFEND_COMMAND)){
            interpretDefendCommand(messageObject, player);
        }
        else if (command.equals(Commands.ATTACK_CAPTURE_COMMAND)){
            //interpretCaptureCommand(messageObject, player);
        }
        else if (command.equals(Commands.FORTIFY_COMMAND)){
            interpretFortifyCommand(messageObject, player);
        }
        else {
            System.out.println("NOT IMPLEMENTED COMMAND: " + command);
            System.out.println(command);
        }

        return null;
    }

    private void interpretPlayCardsCommand(JSONObject commandObject, int player){
        /*{
            "command": "play_cards",
            "payload": {
                "cards": [
                    [1, 2, 3],
                    [4, 5, 6]
                ],
                "armies": 3
            },
            "player_id": 0,
            "ack_id": 1
        }*/

        JSONObject payload = (JSONObject)(commandObject.get("payload"));

        JSONArray cards = (JSONArray)(payload.get("cards"));

        ArrayList<ArrayList<RiskCard>> triplets = new ArrayList<ArrayList<RiskCard>>();
        for (Object tripletObject : cards) {
            JSONArray tripletJSON = (JSONArray) tripletObject;
            ArrayList<RiskCard> triplet = new ArrayList<RiskCard>();
            for (int i = 0; i<tripletJSON.size();i++){
                int cardId = Integer.parseInt(tripletJSON.get(i).toString());
//                triplet.add(gameState.getPlayers().get(player).getRiskCardById(cardId));
            }
            triplets.add(triplet);
        }

        for (ArrayList<RiskCard> triplet : triplets){
            //TradeAction ac = new TradeAction(gameState.getPlayers().get(player),triplet);
            //TODO push
            System.out.println("Interpreted trade command");
        }





    }


    private void interpretAttackCommand(JSONObject commandObject, int player){
        /*{
            "command": "attack",
            "payload": [1,2,1],
            "player_id" : 1
        }*/

        JSONArray attackPlan = (JSONArray)(commandObject.get("payload"));

        int originId = Integer.parseInt(attackPlan.get(0).toString());

        int objectiveId = Integer.parseInt(attackPlan.get(1).toString());

        int attackArmies = Integer.parseInt(attackPlan.get(2).toString());

        AttackActionBuilder builder = new AttackActionBuilder();

        builder.setAttackerId(player);
        builder.setAttackerArmies(attackArmies);
        builder.setObjectiveId(objectiveId);
        builder.setOriginId(originId);
        this.builder =builder;
        System.out.println("Interpreted attack command");




    }

    private void interpretFortifyCommand(JSONObject commandObject, int player){
        /*
        {
            "command": "fortify",
            "payload": [1, 2, 5],
            "player_id": 0,
            "ack_id": 1
        }
        */

        JSONArray fortification = (JSONArray)(commandObject.get("payload"));

        int originId = Integer.parseInt(fortification.get(0).toString());

        int objectiveId = Integer.parseInt(fortification.get(1).toString());

        int armies = Integer.parseInt(fortification.get(2).toString());

        //FortifyAction ac = new FortifyAction(gameState.getPlayers().get(player),gameState.getCountryByID(originId),gameState.getCountryByID(objectiveId),armies);
        System.out.println("Interpreted fortify command");




    }

    private void interpretDrawCardCommand(JSONObject commandObject, int player){
        /*{
            "command": "draw_card",
            "payload": 12,
            "player_id": 0,
            "ack_id": 1
        }*/


        //ObtainRiskCardAction ac = new ObtainRiskCardAction(gameState.getPlayers().get(player));
        System.out.println("Interpreted draw command");




    }

    private void interpretDefendCommand (JSONObject commandObject, int player){
        /*{
            "command": "defend",
            "payload": 2,
            "player_id": 0,
            "ack_id": 1
        }*/

        int amount = Integer.parseInt(commandObject.get("payload").toString());
        builder.setDefenderArmies(amount);
        System.out.println("Interpreted defend command");




    }

    private void interpretTradeCommand(JSONObject commandObject, int player){
        /*{
            "command": "trade_in_cards",
            "payload": [1,2,3],
            "player_id" : 1
        }*/

        ArrayList<RiskCard> cards = new ArrayList<RiskCard>();

        JSONArray cardIds = (JSONArray)(commandObject.get("payload"));

        for (Object id : cardIds){
            cards.add(gameState.getPlayers().get(player).getRiskCardById(Integer.parseInt(id.toString())));
        }

        TradeAction ac = new TradeAction(gameState.getPlayers().get(player),cards);

        //push to client

        //clients.get(player).pushAction(ac);
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
