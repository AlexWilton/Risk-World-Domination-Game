package uk.ac.standrews.cs.cs3099.useri.risk.helpers;


import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.ArrayQueue;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.ac.standrews.cs.cs3099.risk.game.RandomNumbers;
import uk.ac.standrews.cs.cs3099.useri.risk.action.*;
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
import java.security.spec.ECField;
import java.util.*;


/**
 * Created by po26 on 12/02/15.
 */
public class ClientSocketHandler implements Runnable{


    public enum ProtocolState {
        START,
        WAITING_FOR_PING,
        WAITING_FOR_READY,
        WAITING_FOR_INIT,

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



    private int hostId;


    private ProtocolState protocolState;

    private int proclaimedPlayerAmount;


    public ProtocolState getProtocolState (){
        return protocolState;
    }

    public void linkGameState(State state){
        gameState = state;
    }





    public ClientSocketHandler() {
        this.remoteClients = new ArrayList<>();
        protocolState = ProtocolState.START;

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

    public NetworkClient getRemoteClientById(int id){
        for (Client c : remoteClients){
            if (c.getPlayerId() == id){
                return (NetworkClient) c;
            }
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
        localClient.setPlayerName(name);
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


        RNGSeed fpSeed = popSeed();
        RandomNumbers r = new RandomNumbers(fpSeed.getHexSeed());

        int startingPlayer = (r.getRandomByte()+128)%getPlayerAmount();

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
                    case WAITING_FOR_INIT:{
                        processCommandWaitingInit(currentCommand);
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
            processHostPingCommand((PingCommand) command);
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
        else if (command instanceof PingCommand){
            processPingCommand((PingCommand) command);
        }

        else{
            System.out.println("Command ignored:");
            System.out.println(command.toJSONString());
        }
    }
    public void processCommandWaitingInit(Command command){

        if (command instanceof InitialiseGameCommand){
            processInitialiseGameCommand((InitialiseGameCommand) command);
        }
        else {
            System.out.println("Command ignored:");
            System.out.println(command.toJSONString());
        }

    }

    public void processCommandRunning (Command command){

        if (command instanceof AttackCaptureCommand){

        }
        else if (command instanceof AttackCommand){
            processAttackCommand((AttackCommand) command);

        }
        else if (command instanceof DefendCommand){

        }
        else if (command instanceof DeployCommand){
            processDeployCommand((DeployCommand) command);
        }
        else if (command instanceof DrawCardCommand){
            processDrawCardCommand((DrawCardCommand) command);
        }
        else if (command instanceof FortifyCommand){
            processFortifyCommand((FortifyCommand) command);
        }
        else if (command instanceof PlayCardsCommand){
            processPlayCardsCommand((PlayCardsCommand) command);
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

        else if (command instanceof SetupCommand){
            processSetupCommand((SetupCommand) command);
        }
        else {
            System.out.println("Command ignored:");
            System.out.println(command.toJSONString());
        }


    }

    private void processRollCommand(RollCommand command){

    }

    private void processRollHashCommand(RollHashCommand command){
        try {
            while (seed == null) Thread.sleep(10);
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        int player = command.getPlayer();
        String rollHash = command.get("payload").toString();

        seed.addSeedComponentHash(rollHash,player);


    }

    private void processRollNumberCommand(RollNumberCommand command){
        int player = command.getPlayer();
        String rollNumber = command.get("payload").toString();
        seed.addSeedComponent(rollNumber, player);

        seed.addSeedComponent(rollNumber, player);

    }

    private void processPlayersJoinedCommand(PlayersJoinedCommand command){

        JSONArray playersJSON = (JSONArray) command.get("payload");

        for (Object playerObject : playersJSON){
            JSONArray onePlayerJSON = (JSONArray) playerObject;
            int playerNr = Integer.parseInt(onePlayerJSON.get(0).toString());
            if (playerNr == localClient.getPlayerId())
                continue;
            String playerName = onePlayerJSON.get(1).toString();
            String playerSig = null;
            if (onePlayerJSON.size() >2){
                playerSig = onePlayerJSON.get(2).toString();
            }
            //Create the client
            NetworkClient remoteClient = new NetworkClient(gameState);
            remoteClient.setPlayerId(playerNr);
            remoteClient.setPlayerName(playerName);
            System.out.println("Created " + playerName);
            //maybe signature
            remoteClients.add(remoteClient);
        }

    }

    private void processSetupCommand(SetupCommand command){
        int countryId = command.getPayloadAsInt();
        SetupAction act = new SetupAction(gameState.getPlayer(command.getPlayer()),gameState.getCountryByID(countryId));
        getRemoteClientById(command.getPlayer()).pushAction(act);

    }

    private void processHostPingCommand(PingCommand command){
        hostId = command.getPlayer();
        proclaimedPlayerAmount = Integer.parseInt(command.get("payload").toString());

        //TODO supposed to ask for ready
        sendCommand(new PingCommand(localClient.getPlayerId(), null));
        localClient.markPlayReady(true);
        protocolState = ProtocolState.WAITING_FOR_READY;
        processCommandRunning(command);
    }

    private void processPingCommand(PingCommand command){
        getClientById(command.getPlayer()).markPlayReady(true);
    }

    private void processReadyCommand(ReadyCommand command){

        protocolState = ProtocolState.WAITING_FOR_INIT;
    }

    private void processInitialiseGameCommand(InitialiseGameCommand command){

        protocolState = ProtocolState.RUNNING;
    }







    public void sendCommand (Command command){
        out.println(command.toJSONString());
        out.flush();

    }
    public Command getNextCommand () throws IOException{

        String currentIn = "";
        while (StringUtils.countMatches(currentIn,"{") != StringUtils.countMatches(currentIn,"}") || StringUtils.countMatches(currentIn,"{") == 0){
            String nextPart = in.readLine();
            if (currentIn.length() >0 || nextPart.startsWith("{"))
                currentIn += nextPart;
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


    private void processDefendCommand(DefendCommand command) {


        int armies = command.getPayloadAsInt();
        int player = command.getPlayer();

        builder.setDefenderArmies(armies);



    }

    private void processPlayCardsCommand(PlayCardsCommand command){
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

        JSONObject payload = command.getPayload();
        int player = command.getPlayer();

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
            TradeAction act = new TradeAction(gameState.getPlayers().get(player),triplet);
            getRemoteClientById(player).pushAction(act);
            System.out.println("Interpreted trade command");
        }





    }


    private void processAttackCommand(AttackCommand command){
        /*{
            "command": "attack",
            "payload": [1,2,1],
            "player_id" : 1
        }*/

        JSONArray attackPlan = command.getPayloadAsArray();
        int player = command.getPlayer();

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

    private void processFortifyCommand(FortifyCommand command){
        /*
        {
            "command": "fortify",
            "payload": [1, 2, 5],
            "player_id": 0,
            "ack_id": 1
        }
        */

        JSONArray fortification = command.getPayloadAsArray();
        int player = command.getPlayer();

        int originId = Integer.parseInt(fortification.get(0).toString());

        int objectiveId = Integer.parseInt(fortification.get(1).toString());

        int armies = Integer.parseInt(fortification.get(2).toString());

        FortifyAction act = new FortifyAction(gameState.getPlayers().get(player),gameState.getCountryByID(originId),gameState.getCountryByID(objectiveId),armies);
        System.out.println("Interpreted fortify command");
        getRemoteClientById(player).pushAction(act);




    }

    private void processDrawCardCommand(DrawCardCommand command){
        /*{
            "command": "draw_card",
            "payload": 12,
            "player_id": 0,
            "ack_id": 1
        }*/

        int player = command.getPlayer();
        ObtainRiskCardAction act = new ObtainRiskCardAction(gameState.getPlayers().get(player));
        System.out.println("Interpreted draw command");
        getRemoteClientById(player).pushAction(act);




    }

    private void processDeployCommand(DeployCommand command) {

        JSONArray territoryNumberPairs = command.getPayloadAsArray();
        int player = command.getPlayer();
        //construct amount of deploy actions
        for (Object pair : territoryNumberPairs) {

            //extract parameters
            JSONArray pairArray = (JSONArray) pair;
            int countryId = Integer.parseInt(pairArray.get(0).toString());
            int armies = Integer.parseInt(pairArray.get(1).toString());

            //create deploy action
            DeployArmyAction act = new DeployArmyAction(gameState.getPlayers().get(player),gameState.getCountryByID(countryId),armies);

            //push to client

            getRemoteClientById(player).pushAction(act);

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



    public RNGSeed popSeed(){

        localClient.newSeedComponent();

        seed = new RNGSeed(getPlayerAmount());
        seed.addSeedComponentHash(localClient.getHexSeedHash(),localClient.getPlayerId());
        seed.addSeedComponent(localClient.getHexSeed(),localClient.getPlayerId());
        sendCommand(new RollHashCommand(localClient.getHexSeedHash(),localClient.getPlayerId()));

        try {
            while (!seed.hasAllHashes()) Thread.sleep(10);
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("has all hashes");

        sendCommand(new RollNumberCommand(localClient.getHexSeed(), localClient.getPlayerId()));


        try {
            while (!seed.hasAllNumbers()) Thread.sleep(10);
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("has all numbers");
        RNGSeed ret = seed;
        seed = null;
        return ret;
    }





}
