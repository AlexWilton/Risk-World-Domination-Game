package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.ac.standrews.cs.cs3099.risk.game.RandomNumbers;
import uk.ac.standrews.cs.cs3099.useri.risk.action.*;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.RNGSeed;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.RiskCard;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.*;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.exceptions.RollException;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * Class only for forwarding messages from all clients to this client and vice versa.
 */
public class HostForwarder {
    private static State state;
    private static RNGSeed seed;

    private MessageQueue messageQueue;
    private final int MOVE_TIMEOUT;
    private final int ACK_TIMEOUT;
    private final int ID;
    private BufferedReader input;
    private boolean ack_received;
    private boolean move_required;

    private double timer = System.currentTimeMillis();
    private double diff;
    private int last_ack = 0;

    public HostForwarder(MessageQueue q, int move_timeout, int ack_timeout, int id, BufferedReader input) {
        messageQueue = q;
        MOVE_TIMEOUT = move_timeout;
        ACK_TIMEOUT = ack_timeout;
        ID = id;
        this.input = input;
        diff = MOVE_TIMEOUT;
    }

    public static void setState(State state) {
        HostForwarder.state = state;
    }

    public static void setSeed(RNGSeed seed) {
        HostForwarder.seed = seed;
    }

    void getFirstPlayer() throws IOException {
        Command comm = Command.parseCommand(input.readLine());
        messageQueue.sendAll(comm, ID);
        if (!(comm instanceof RollHashCommand)) {
            throw new RollException();
        }
        RollHashCommand hash = (RollHashCommand) comm;
        String hashStr = hash.get("payload").toString();
        seed.addSeedComponentHash(hashStr, ID);

        while (!seed.hasAllHashes());

        comm = Command.parseCommand(input.readLine());
        messageQueue.sendAll(comm, ID);
        if (!(comm instanceof RollNumberCommand)) {
            throw new RollException();
        }
        RollNumberCommand roll = (RollNumberCommand) comm;
        String rollStr = roll.get("payload").toString();
        seed.addSeedComponent(rollStr, ID);

        //seed = null;
    }

    void shuffleDeck() throws IOException {
        Command comm = Command.parseCommand(input.readLine());
        messageQueue.sendAll(comm, ID);
        if (!(comm instanceof RollHashCommand)) {
            throw new RollException();
        }
        RollHashCommand hash = (RollHashCommand) comm;
        String hashStr = hash.get("payload").toString();
        seed.addSeedComponentHash(hashStr, ID);

        while (!seed.hasAllHashes());

        comm = Command.parseCommand(input.readLine());
        messageQueue.sendAll(comm, ID);
        if (!(comm instanceof RollNumberCommand)) {
            throw new RollException();
        }
        RollNumberCommand roll = (RollNumberCommand) comm;
        String rollStr = roll.get("payload").toString();
        seed.addSeedComponent(rollStr, ID);
    }

    void playGame() throws IOException {
        //System.err.println(state.getCurrentPlayer().getID() == state.getFirstPlayer().getID());
        while(true) {
            if (move_required && System.currentTimeMillis() > timer + MOVE_TIMEOUT) {
                System.err.println("Mov from " + ID + " timed out");
                throw new SocketTimeoutException();
            }
            if (!ack_received && System.currentTimeMillis() > timer + ACK_TIMEOUT) {
                System.err.println("Ack from " + ID + " timed out");
                throw new SocketTimeoutException();
            }
            if (!move_required && state.getCurrentPlayer().getID() == ID){
                move_required = true;
                System.out.println("Player " + ID + "'s turn'");
                timer = System.currentTimeMillis();
            }
            if (input.ready()) {
                Command reply = Command.parseCommand(input.readLine());
                System.out.println("in Player " + ID + ": " + reply);
                messageQueue.sendAll(reply, ID);
                checkAckCases(reply);

            }
        }
    }

    private void checkAckCases(Command comm) {
        if (comm instanceof AcknowledgementCommand) {
            if (((AcknowledgementCommand) comm).getAcknowledgementId() == last_ack) {
                ack_received = true;
                timer = System.currentTimeMillis();
                //diff = MOVE_TIMEOUT;
            }
        } else {
            processCommand(comm);

        }
    }

    private void processCommand(Command currentCommand) {
        Player currentPlayer = state.getCurrentPlayer();
        ArrayList<Action> playerActions = new ArrayList<>();
        if (currentCommand instanceof AttackCommand){
            playerActions.add(processAttackCommand((AttackCommand) currentCommand));
        }
        else if (currentCommand instanceof DeployCommand){
            playerActions.addAll(processDeployCommand((DeployCommand) currentCommand));
        }
        else if (currentCommand instanceof FortifyCommand){
            playerActions.add(processFortifyCommand((FortifyCommand) currentCommand));
        }
        else if (currentCommand instanceof DrawCardCommand){
            playerActions.add(processDrawCardCommand((DrawCardCommand) currentCommand));
        }
        else if (currentCommand instanceof SetupCommand){
            playerActions.add(processSetupCommand((SetupCommand) currentCommand));
        }
        else if (currentCommand instanceof PlayCardsCommand){
            playerActions.addAll(processPlayCardsCommand((PlayCardsCommand) currentCommand));
        }
        else {
            System.out.println("cant process command " + currentCommand.toJSONString());
            return;
        }

        if (playerActions.size() == 0){
            System.out.println("End turn");
            playerActions.add(new FortifyAction(currentPlayer));
        }

        for(Action playerAction : playerActions) {
            if (playerAction.validateAgainstState(state)) {
                playerAction.performOnState(state);
            } else {
                System.out.println("Error move did not validate: " + currentCommand);
                System.exit(1);
            }
        }

        if(state.winConditionsMet()){
            Player winner = state.getWinner();

            System.out.println("Winner is " + winner.getID());
            //TODO follow endGame protocol
            System.exit(0);
        }
        move_required = false;
        //state.nextPlayer();
        timer = System.currentTimeMillis();
    }

    private ArrayList<TradeAction> processPlayCardsCommand(PlayCardsCommand command){
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
                triplet.add(state.getPlayers().get(player).getRiskCardById(cardId));
            }
            triplets.add(triplet);
        }

        ArrayList<TradeAction> acts = new ArrayList<>();
        for (ArrayList<RiskCard> triplet : triplets){
            acts.add(new TradeAction(state.getPlayers().get(player),triplet));
        }

        return acts;
    }


    private AttackAction processAttackCommand(AttackCommand command){
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

        // TODO Need to pull the defendCommand from somewhere, but how?
        DefendCommand def = state.getCountryByID(objectiveId).getOwner().getClient().popDefendCommand(originId,objectiveId,attackArmies);
        //if its local, propagate
        if (state.getCountryByID(objectiveId).getOwner().getClient().isLocal()) {
            //csh.sendCommand(def);
        }
        int defendArmies = def.getPayloadAsInt();
        RandomNumbers rng = new RandomNumbers(seed.getHexSeed());

        int[] attackDice = new int [attackArmies];
        for (int i = 0; i<attackArmies; i++){
            attackDice[i] = (rng.getRandomByte()+128)%6;
        }

        int[] defendDice = new int [defendArmies];
        for (int i = 0; i<defendArmies; i++){
            defendDice[i] = (rng.getRandomByte()+128)%6;
        }

        AttackAction act = new AttackAction(state.getPlayer(player),state.getCountryByID(originId),state.getCountryByID(objectiveId),attackDice,defendDice);

        return act;
    }

    private FortifyAction processFortifyCommand(FortifyCommand command){
        /*
        {
            "command": "fortify",
            "payload": [1, 2, 5],
            "player_id": 0,
            "ack_id": 1
        }
        */

        if (command.get("payload") == null){
            return new FortifyAction(state.getPlayer(command.getPlayer()));
        }
        JSONArray fortification = command.getPayloadAsArray();
        int player = command.getPlayer();
        int originId = Integer.parseInt(fortification.get(0).toString());
        int objectiveId = Integer.parseInt(fortification.get(1).toString());
        int armies = Integer.parseInt(fortification.get(2).toString());

        FortifyAction act = new FortifyAction(state.getPlayers().get(player),state.getCountryByID(originId),state.getCountryByID(objectiveId),armies);
        System.out.println("Interpreted fortify command");

        return act;


    }

    private ObtainRiskCardAction processDrawCardCommand(DrawCardCommand command){
        /*{
            "command": "draw_card",
            "payload": 12,
            "player_id": 0,
            "ack_id": 1
        }*/

        int player = command.getPlayer();
        ObtainRiskCardAction act = new ObtainRiskCardAction(state.getPlayers().get(player));
        System.out.println("Interpreted draw command");
        return act;
    }

    private ArrayList<DeployArmyAction> processDeployCommand(DeployCommand command) {
        ArrayList<DeployArmyAction> deployArmyActions = new ArrayList<>();
        JSONArray territoryNumberPairs = command.getPayloadAsArray();
        int player = command.getPlayer();
        //construct amount of deploy actions
        for (Object pair : territoryNumberPairs) {
            //extract parameters
            JSONArray pairArray = (JSONArray) pair;
            int countryId = Integer.parseInt(pairArray.get(0).toString());
            int armies = Integer.parseInt(pairArray.get(1).toString());

            //create deploy action and add it to action list
            deployArmyActions.add(new DeployArmyAction(state.getPlayer(player),state.getCountryByID(countryId),armies));
        }
        return deployArmyActions;
    }

    private SetupAction processSetupCommand(SetupCommand command){
        int countryId = command.getPayloadAsInt();
        SetupAction act = new SetupAction(state.getPlayer(command.getPlayer()),state.getCountryByID(countryId));
        return act;

    }

    public void signalAck(int ack_id) {
        last_ack = ack_id;
        timer = System.currentTimeMillis();
        ack_received = false;
    }

    boolean hasSeed() {
        return seed != null;
    }
}
