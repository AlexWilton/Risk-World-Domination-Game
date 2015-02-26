package uk.ac.standrews.cs.cs3099.useri.risk.helpers;


import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import uk.ac.standrews.cs.cs3099.useri.risk.action.AttackAction;
import uk.ac.standrews.cs.cs3099.useri.risk.action.DeployArmyAction;
import uk.ac.standrews.cs.cs3099.useri.risk.action.TradeAction;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.NetworkClient;
import uk.ac.standrews.cs.cs3099.useri.risk.game.RiskCard;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;


import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by po26 on 12/02/15.
 */
public class ClientSocketDistributor implements Runnable{


    private ArrayList<NetworkClient> clients;

    private Socket clientSocket;

    private State gameState;

    private ArrayList<AttackActionBuilder> builders;


    public ClientSocketDistributor(Socket clientSocket, ArrayList<NetworkClient> clients){
        this.clients = clients;
        this.clientSocket = clientSocket;
        this.builders = new ArrayList<AttackActionBuilder>();
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
            interpretDeployCommand(messageObject, player);
        }
        else if (command.equals(Commands.ATTACK_COMMAND)){
            interpretAttackCommand(messageObject, player);
        }
        else if (command.equals(Commands.PLAY_CARDS_COMMAND)){
            interpretPlayCardsCommand(messageObject, player);
        }
        else if (command.equals(Commands.DRAW_CARD_COMMAND)){
            //interpretDrawCardCommand(messageObject, player);
        }
        else if (command.equals(Commands.DEFEND_COMMAND)){
            //interpretDefendCommand(messageObject, player);
        }
        else if (command.equals(Commands.ATTACK_CAPTURE_COMMAND)){
            //interpretCaptureCommand(messageObject, player);
        }
        else if (command.equals(Commands.FORTIFY_COMMAND)){
            //interpretFortifyCommand(messageObject, player);
        }
        else {
            System.out.println("NOT IMPLEMENTED COMMAND: " + command);
            System.out.println(message);
        }
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
                triplet.add(gameState.getPlayers().get(player).getRiskCardById(cardId));
            }
            triplets.add(triplet);
        }

        for (ArrayList<RiskCard> triplet : triplets){
            TradeAction ac = new TradeAction(gameState.getPlayers().get(player),triplet);
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
        builders.add(builder);
        System.out.println("Interpreted attack command");




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
