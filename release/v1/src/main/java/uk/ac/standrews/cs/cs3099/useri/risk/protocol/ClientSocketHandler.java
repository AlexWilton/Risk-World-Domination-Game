package uk.ac.standrews.cs.cs3099.useri.risk.protocol;


import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.NetworkClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.webClient.WebClient;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.RandomNumberGenerator;
import uk.ac.standrews.cs.cs3099.useri.risk.main.ClientApp;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


/**
 * Class to handle the socket on the client side
 */
public class ClientSocketHandler implements Runnable {

    private WebClient webClientForWatchOnlyServer = null;
    private HashMap<Integer,HashMap<Integer,Boolean>> ackRecieved;



    public int getLocalClientId(){
        return localClient.getPlayerId();
    }

    /**
     * removes all links to a player, terminates if its this player
     * @param id
     */
    public void removePlayer(int id) {
        if (id == localClient.getPlayerId()) {
            System.out.println("this player lost");

        } else {
            gameState.removePlayer(id);
            remoteClients.remove(getRemoteClientById(id));
            System.out.println("player " + id + "lost");

        }
    }

    /**
     * gives the client a watch only status
     * @param webClient
     */
    public void setWebClientForWatchOnlyServer(WebClient webClient) {
        webClientForWatchOnlyServer = webClient;
    }

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

    private RandomNumberGenerator seed;

    private PrintWriter out;

    private BufferedReader in;


    private int hostId;


    private ProtocolState protocolState;

    private int proclaimedPlayerAmount;


    public ProtocolState getProtocolState() {
        return protocolState;
    }

    /**
     * links gamestate to local player
     * @param state
     */
    public void linkGameState(State state) {
        gameState = state;
        localClient.setState(state);
        if(webClientForWatchOnlyServer != null)
            webClientForWatchOnlyServer.setState(state);
    }


    public ClientSocketHandler() {
        this.remoteClients = new ArrayList<>();
        protocolState = ProtocolState.START;
        ackRecieved = new HashMap<>();

    }

    /**
     * get all clients
     * @return
     */
    public ArrayList<Client> getAllClients() {
        ArrayList<Client> ret = new ArrayList<>();

        ret.add(localClient);
        ret.addAll(remoteClients);

        return ret;
    }

    Client getClientById(int id) {
        for (Client c : remoteClients) {
            if (c.getPlayerId() == id) {
                return c;
            }
        }
        if (localClient.getPlayerId() == id) {
            return localClient;
        }

        return null;
    }

    public NetworkClient getRemoteClientById(int id) {
        for (Client c : remoteClients) {
            if (c.getPlayerId() == id) {
                return (NetworkClient) c;
            }
        }
        return null;
    }

    /**
     * connects this local client and also joins the game
     *
     * @param address
     * @param port
     * @param localClient
     * @param versions
     * @param features
     * @return state of client app
     */

    public int initialise(String address, int port, Client localClient, float[] versions, String[] features, String name) {
        //try to connect
        this.localClient = localClient;
        localClient.setPlayerName(name);
        try {
            clientSocket = new Socket(address, port);
            //make the writers/Readers
            out =
                    new PrintWriter(clientSocket.getOutputStream(), true);
            in =
                    new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));

        } catch (IOException e) {
            System.out.println("Can't connect to server");
            return ClientApp.BAD_ADDRESS;
        }

        try {
            //send join message
            JSONArray versionsJSON = new JSONArray();
            for (float version : versions) {
                versionsJSON.add(version);
            }
            JSONArray featuresJSON = new JSONArray();
            Collections.addAll(featuresJSON, features);
            JoinGameCommand joinCommand = new JoinGameCommand(versionsJSON, featuresJSON, name);
            sendCommand(joinCommand);


            //wait for accept or join
            //run as long as connection is up
            boolean replied = false;
            while (!replied) {
                Command reply = getNextCommand();
                if (reply instanceof AcceptJoinGameCommand) {
                    //Fill in details for starting the game
                    //make the local client+player
                    String playerIdString = ((JSONObject) reply.get("payload")).get("player_id").toString();
                    localClient.setPlayerId(Integer.parseInt(playerIdString));
                    replied = true;
                    System.out.println("Joined Game!");
                } else if (reply instanceof RejectJoinGameCommand) {
                    //was rejected
                    System.out.println("Was rejected!");
                    protocolState = ProtocolState.REJECTED;
                    return ClientApp.JOIN_REJECTED;
                } else {
                    System.out.println("Wrong Protocol");
                    protocolState = ProtocolState.FAILED;
                    return ClientApp.PROTOCOL_ERROR_DETECTED;
                }
            }
            protocolState = ProtocolState.WAITING_FOR_PING;
            return ClientApp.SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("wrong");
            protocolState = ProtocolState.FAILED;
            return ClientApp.COMMUNICATION_FAILED;
        }


    }

    public int determineFirstPlayer() {


        RandomNumberGenerator fpSeed = popSeed();
        return (int) (fpSeed.nextInt() % getPlayerAmount());


    }

    private int getPlayerAmount() {
        return remoteClients.size() + 1;
    }

    @Override
    public void run() {
        //gets commands and processes / queues them

        while (true) {
            try {
                Command currentCommand = getNextCommand();
                switch (protocolState) {
                    case WAITING_FOR_PING: {
                        processCommandWaitingPing(currentCommand);
                    }
                    break;
                    case WAITING_FOR_READY: {
                        processCommandWaitingReady(currentCommand);
                    }
                    break;
                    case WAITING_FOR_INIT: {
                        processCommandWaitingInit(currentCommand);
                    }
                    break;
                    case RUNNING: {
                        processCommandRunning(currentCommand);
                    }
                    break;
                    default: {
                        System.out.println("UNEXPECTED");
                        System.exit(-1);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * processes command while waiting for ping phase
     * @param command
     */
    void processCommandWaitingPing(Command command) {
        if (command instanceof PlayersJoinedCommand) {
            processPlayersJoinedCommand((PlayersJoinedCommand) command);
        } else if (command instanceof PingCommand) {
            processHostPingCommand((PingCommand) command);
        } else if (command instanceof AcknowledgementCommand) {
            processAcknowledgenmentCommand((AcknowledgementCommand) command);
        }else {
            System.out.println("Command ignored: " + command.toJSONString());
        }
    }

    /**
     * processes command while waiting for ready command
     * @param command
     */
    void processCommandWaitingReady(Command command) {
        if (command instanceof ReadyCommand) {
            processReadyCommand((ReadyCommand) command);
        } else if (command instanceof PingCommand) {
            processPingCommand((PingCommand) command);
        } else if (command instanceof AcknowledgementCommand) {
            processAcknowledgenmentCommand((AcknowledgementCommand) command);
        }else {
            System.out.println("Command ignored: " + command.toJSONString());
        }
    }

    /**
     * process command while waiting for init
     * @param command
     */
    void processCommandWaitingInit(Command command) {

        if (command instanceof InitialiseGameCommand) {
            processInitialiseGameCommand((InitialiseGameCommand) command);
        } else if (command instanceof AcknowledgementCommand) {
            processAcknowledgenmentCommand((AcknowledgementCommand) command);
        } else {
            System.out.println("Command ignored: " + command.toJSONString());
        }

    }

    /**
     * process command while normal operation
     * @param command
     */
    void processCommandRunning(Command command) {

        if (command instanceof RollHashCommand) {
            processRollHashCommand((RollHashCommand) command);
        } else if (command instanceof RollNumberCommand) {
            processRollNumberCommand((RollNumberCommand) command);
        } else if (command instanceof AcknowledgementCommand) {
            processAcknowledgenmentCommand((AcknowledgementCommand) command);
        } else {
            Client c = getClientById(command.getPlayer());
            if (c != null)
                c.pushCommand(command);
        }


    }

    void processAcknowledgenmentCommand(AcknowledgementCommand command){
        if (!ackRecieved.keySet().contains(command.getPayloadAsInt())){
            HashMap <Integer,Boolean> rec = new HashMap<>();
            for (Client c : getAllClients()) {
                rec.put(c.getPlayerId(),false);
            }
            ackRecieved.put(command.getPayloadAsInt(), rec);
        }

        ackRecieved.get(command.getPayloadAsInt()).put(command.getPlayer(),true);

    }


    private void processRollHashCommand(RollHashCommand command) {

        String rollHash = command.get("payload").toString();

        getClientById(command.getPlayer()).pushRollHash(rollHash);


    }

    private void processRollNumberCommand(RollNumberCommand command) {
        String rollNumber = command.get("payload").toString();
        getClientById(command.getPlayer()).pushRollNumber(rollNumber);

    }

    /**
     * add newly joined players to list
     * @param command
     */
    private void processPlayersJoinedCommand(PlayersJoinedCommand command) {

        JSONArray playersJSON = (JSONArray) command.get("payload");

        for (Object playerObject : playersJSON) {
            JSONArray onePlayerJSON = (JSONArray) playerObject;
            int playerNr = Integer.parseInt(onePlayerJSON.get(0).toString());
            if (playerNr == localClient.getPlayerId() || getClientById(playerNr) != null)
                continue;
            String playerName = onePlayerJSON.get(1).toString();
            String playerSig = null;
            if (onePlayerJSON.size() > 2) {
                playerSig = onePlayerJSON.get(2).toString();
            }
            //Create the client
            NetworkClient remoteClient = new NetworkClient(gameState, seed);
            remoteClient.setPlayerId(playerNr);
            remoteClient.setPlayerName(playerName);
            //System.out.println("Created " + playerName);
            //maybe signature
            remoteClients.add(remoteClient);
        }

    }


    /**
     * proc hosts ping at start
     */
    private void processHostPingCommand(PingCommand command) {
        hostId = command.getPlayer();
        proclaimedPlayerAmount = Integer.parseInt(command.get("payload").toString());

        //TODO supposed to ask for ready
        sendCommand(new PingCommand(localClient.getPlayerId(), null));
        localClient.markPlayReady();
        protocolState = ProtocolState.WAITING_FOR_READY;
        processCommandRunning(command);
    }

    private void processPingCommand(PingCommand command) {
        // getClientById(command.getPlayer()).markPlayReady(true);
    }

    private void processReadyCommand(ReadyCommand command) {

        protocolState = ProtocolState.WAITING_FOR_INIT;
    }

    private void processInitialiseGameCommand(InitialiseGameCommand command) {

        protocolState = ProtocolState.RUNNING;
    }

    /**
     * sends out command
     * @param command
     */
    public void sendCommand(Command command) {


        try {
            out.println(command.toJSONString());
            if (command.requiresAcknowledgement())
                Command.increaseAckId();
        }catch (NullPointerException e){
            System.err.println("what");
        }
        System.out.println("Sent to server: " + command.toJSONString());
        out.flush();

        //if it needs ack, wait
        if (command.requiresAcknowledgement()){


            if (!ackRecieved.keySet().contains(command.getAck())){
                HashMap <Integer,Boolean> rec = new HashMap<>();
                for (Client c : getAllClients()){
                    rec.put(c.getPlayerId(),false);
                }
                ackRecieved.put(command.getAck(),rec);
            }

            ackRecieved.get(command.getAck()).put(command.getPlayer(),true);

        }

    }

    /**
     * gets the nedxt command, sends ack and queues it
     * @return
     * @throws IOException
     */
    synchronized Command getNextCommand() throws IOException {

        String currentIn = "";
        while (StringUtils.countMatches(currentIn, "{") != StringUtils.countMatches(currentIn, "}") || StringUtils.countMatches(currentIn, "{") == 0) {
            if (in.ready()) {
                String nextPart = in.readLine();
                if (currentIn.length() > 0 || nextPart.startsWith("{"))
                    currentIn += nextPart;
            } else {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        Command command = Command.parseCommand(currentIn);
        //System.out.println("recieved: " + currentIn);

        //Acknowledge if required
        if (command.containsKey("ack_id")) {
            int ackId = Integer.parseInt(command.get("ack_id").toString());
            AcknowledgementCommand ack = new AcknowledgementCommand(ackId, localClient.getPlayerId());
            out.println(ack);
            out.flush();
        }

        return command;
    }

    /**
     * gets rng seed and rng from all players
     * @return
     */
    public RandomNumberGenerator popSeed() {



        try {
            while (seed != null) Thread.sleep(10);
            localClient.newSeedComponent();
            seed = new RandomNumberGenerator();
            seed.addHash(localClient.getPlayerId(), localClient.getHexSeedHash());

            sendCommand(new RollHashCommand(localClient.getHexSeedHash(), localClient.getPlayerId()));

            for (Client c : getAllClients()) {
                if (c.getPlayerId() == localClient.getPlayerId()) {
                    continue;
                }
                String hash = c.popRollHash();
                seed.addHash(c.getPlayerId(), hash);
            }

            seed.addNumber(localClient.getPlayerId(), localClient.getHexSeed());

            sendCommand(new RollNumberCommand(localClient.getHexSeed(), localClient.getPlayerId()));


            for (Client c : getAllClients()) {
                if (c.getPlayerId() == localClient.getPlayerId()) {
                    continue;
                }
                String number = c.popRollNumber();

                seed.addNumber(c.getPlayerId(), number);
            }

            seed.finalise();
            RandomNumberGenerator ret = seed;
            seed = null;
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }
}
