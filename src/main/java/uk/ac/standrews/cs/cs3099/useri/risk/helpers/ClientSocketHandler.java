package uk.ac.standrews.cs.cs3099.useri.risk.helpers;


import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.ArrayQueue;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.ac.standrews.cs.cs3099.risk.game.RandomNumbers;
import uk.ac.standrews.cs.cs3099.useri.risk.action.TradeAction;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.NetworkClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.RNGSeed;
import uk.ac.standrews.cs.cs3099.useri.risk.game.ClientApp;
import uk.ac.standrews.cs.cs3099.useri.risk.game.RiskCard;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;
import java.util.*;


/**
 * Created by po26 on 12/02/15.
 */
public class ClientSocketHandler implements Runnable{


    public enum ProtocolState {
        START,
        WAITING_FOR_PING,
        WAITING_FOR_READY,
        WAITING_FOR_ALL_ACKS,

        RUNNING,
        REJECTED,
        FAILED
    }

    private ArrayList<NetworkClient> remoteClients;

    private Client localClient;

    private Socket clientSocket;

    private State gameState;

    private AttackActionBuilder builder;

    private RNGSeed seed;

    private PrintWriter out;

    private BufferedReader in;

    private Deque<RNGSeed> queuedSeeds;


    private int hostId;

    private int diceFaces, diceNumber;

    private ProtocolState protocolState;

    private int proclaimedPlayerAmount;


    public int getDiceFaces(){
        return diceFaces;
    }

    public int getDiceNumber(){
        return diceNumber;
    }

    public ProtocolState getProtocolState (){
        return protocolState;
    }





    public ClientSocketHandler() {
        this.remoteClients = new ArrayList<>();
        protocolState = ProtocolState.START;
        queuedSeeds = new ArrayDeque<>();
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

    /**
     *
     * connects this local client and also joins the game
     * @param address
     * @param port
     * @param localClient
     * @param versions
     * @param features
     * @return
     */

    public int initialise(String address, int port, Client localClient, float[] versions, String[] features, String name){
        //try to connect
        this.localClient = localClient;
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
            JoinGameCommand joinCommand = new JoinGameCommand(versionsJSON,featuresJSON,name);
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
                    protocolState = ProtocolState.REJECTED;
                    return ClientApp.JOIN_REJECTED;
                }
                else {
                    System.out.println("Wromg Protocol");
                    protocolState = ProtocolState.FAILED;
                    return ClientApp.PROTOCOL_ERROR_DETECTED;
                }
            }
            protocolState = ProtocolState.WAITING_FOR_PING;
            return ClientApp.SUCCESS;
        }
        catch (IOException e){
            e.printStackTrace();
            System.out.println("wrong");
            protocolState = ProtocolState.FAILED;
            return ClientApp.COMMUNICATION_FAILED;
        }



/*
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

            sendCommand(new AcknowledgementCommand(ackId, localClient.getPlayerId()));

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

            //Create clients



        }
        catch (IOException e){
            e.printStackTrace();
            System.out.println("wrong");

            return ClientApp.COMMUNICATION_FAILED;
        }*/


    }

    public int determineFirstPlayer(){


        RNGSeed fpSeed = popOldestSeed();
        RandomNumbers r = new RandomNumbers(fpSeed.getHexSeed());

        int startingPlayer = r.getRandomInt()%getDiceFaces();

        return startingPlayer;


    }

    private int getPlayerAmount(){
        return remoteClients.size()+1;
    }

    public boolean allRemoteClientsReady(){

        for (NetworkClient c : remoteClients){
            if (!c.isReady())
                return false;
        }

        return true;
    }

    public void run (){
        while (true) {
            try {
                Command currentCommand = getNextCommand();
                switch (protocolState){
                    case WAITING_FOR_PING:{
                        processCommandWaitingPing(currentCommand);
                    } break;
                    case WAITING_FOR_READY:{
                        processCommandWaitingReady(currentCommand);
                    } break;
                    case WAITING_FOR_ALL_ACKS:{
                        processCommandWaitingAck(currentCommand);
                    } break;
                    case RUNNING:{
                        processCommandRunning(currentCommand);
                    } break;
                    default:{
                        System.out.println("UNEXPECTED");
                        System.exit(-1);
                    }
                }

            }
            catch (IOException e){
                e.printStackTrace();
                System.exit(0);
            }

        }

    }

    public void processCommandWaitingPing(Command command){
        if (command instanceof PlayersJoinedCommand){
            processPlayersJoinedCommand((PlayersJoinedCommand) command);
        }
        else if (command instanceof PingCommand){
            processHostPingCommand((RollNumberCommand) command);
        }
        else{
            System.out.println("Command ignored:");
            System.out.println(command.toJSONString());
        }
    }

    public void processCommandWaitingReady(Command command) {
        if (command instanceof ReadyCommand){
            processReadyCommand((ReadyCommand) command);
        }

        else{
            System.out.println("Command ignored:");
            System.out.println(command.toJSONString());
        }
    }
    public void processCommandWaitingAck(Command command){

        System.out.println("Command ignored:");
        System.out.println(command.toJSONString());

    }

    public void processCommandRunning (Command command){

        if (command instanceof AttackCaptureCommand){

        }
        else if (command instanceof AttackCommand){

        }
        else if (command instanceof DefendCommand){

        }
        else if (command instanceof DeployCommand){

        }
        else if (command instanceof DrawCardCommand){

        }
        else if (command instanceof FortifyCommand){

        }
        else if (command instanceof PlayCardsCommand){

        }
        else if (command instanceof AcknowledgementCommand){

        }
        else if (command instanceof RollCommand){
            processRollCommand((RollCommand) command);
        }
        else if (command instanceof RollHashCommand){
            processRollHashCommand((RollHashCommand) command);
        }
        else if (command instanceof RollNumberCommand){
            processRollNumberCommand((RollNumberCommand) command);
        }
        else {
            System.out.println("Command ignored:");
            System.out.println(command.toJSONString());
        }


    }

    private void processRollCommand(RollCommand command){
        diceNumber = Integer.parseInt(command.getPayload().get("dice_count").toString());
        diceFaces = Integer.parseInt(command.getPayload().get("dice_faces").toString());

        localClient.newSeedComponent();

        //queue old seed
        if (seed != null){
            if (seed.hasAllNumbers()){
                queuedSeeds.push(seed);
            }
        }

        seed = new RNGSeed(getPlayerAmount());
        seed.addSeedComponentHash(localClient.getHexSeedHash(),localClient.getPlayerId());
        seed.addSeedComponent(localClient.getHexSeed(),localClient.getPlayerId());
    }

    private void processRollHashCommand(RollHashCommand command){
        int player = command.getPlayer();
        String rollHash = command.get("payload").toString();
        seed.addSeedComponentHash(rollHash,player);
    }

    private void processRollNumberCommand(RollNumberCommand command){
        int player = command.getPlayer();
        String rollNumber = command.get("payload").toString();
        seed.addSeedComponent(rollNumber, player);
    }

    private void processPlayersJoinedCommand(PlayersJoinedCommand command){

        JSONArray playersJSON = (JSONArray) command.get("payload");

        for (Object playerObject : playersJSON){
            JSONArray onePlayerJSON = (JSONArray) playerObject;
            int playerNr = Integer.parseInt(onePlayerJSON.get(0).toString());
            String playerName = onePlayerJSON.get(1).toString();
            String playerSig = null;
            if (onePlayerJSON.size() >2){
                playerSig = onePlayerJSON.get(2).toString();
            }
            //Create the client
            NetworkClient remoteClient = new NetworkClient();
            remoteClient.setPlayerId(playerNr);
            remoteClient.setPlayerName(playerName);
            //maybe signature
            remoteClients.add(remoteClient);
        }

    }

    private void processHostPingCommand(RollNumberCommand command){
        hostId = command.getPlayer();
        proclaimedPlayerAmount = Integer.parseInt(command.get("payload").toString());
        //TODO supposed to ask for ready
        sendCommand(new PingCommand(localClient.getPlayerId(),null));
        protocolState = ProtocolState.WAITING_FOR_READY;
    }

    private void processReadyCommand(ReadyCommand command){
        protocolState = ProtocolState.RUNNING;
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

        //Acknowledge if required
        if (command.containsKey("ack_id")){
            int ackId = Integer.parseInt(command.get("ack_id").toString());
            AcknowledgementCommand ack = new AcknowledgementCommand(ackId,localClient.getPlayerId());
            sendCommand(ack);
        }
        return command;
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

    public RNGSeed getOldestRNGSeed(){
        //Queued first
        if (queuedSeeds.size() > 0)
            return queuedSeeds.peek();
        while(seed == null);
        while(!seed.hasAllNumbers());
        return seed;
    }

    public RNGSeed popOldestSeed(){
        //Queued first
        if (queuedSeeds.size() > 0)
            return queuedSeeds.pop();
        while(seed == null);
        while(!seed.hasAllNumbers());
        RNGSeed ret = seed;
        seed = null;
        return ret;
    }





}
