package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.ac.standrews.cs.cs3099.useri.risk.action.*;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.CommandQueuer;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.WebClient;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.RiskCard;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.HashMismatchException;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.RandomNumberGenerator;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.*;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.exceptions.RollException;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Queue;

/**
 * Class for forwarding messages and applying the command from a single client to the GameState.
 * Includes methods to parse protocol commands and apply them to the state, and to get and send
 * Diceroll-related commands. Also performs checking for timeouts when an acknowledgement or a
 * move is required.
 */
class HostForwarder implements Runnable{
    private static State state;
    private static RandomNumberGenerator seed;

    private MessageQueue messageQueue;
    private final int MOVE_TIMEOUT;
    private final int ACK_TIMEOUT;
    private final int ID;
    private BufferedReader input;
    private boolean ack_received;
    private boolean move_required;

    private double timer = System.currentTimeMillis();
    private int last_ack = 0;
    private boolean getrolls;
    private boolean running = true;

    private CommandQueuer queuer;
    Thread parserThread;

    public HostForwarder(MessageQueue q, int move_timeout, int ack_timeout, int id, BufferedReader input) {
        messageQueue = q;
        MOVE_TIMEOUT = move_timeout;
        ACK_TIMEOUT = ack_timeout;
        ID = id;
        this.input = input;
        this.queuer = new CommandQueuer();
        //make the parser thread
        parserThread = new Thread(this);
        parserThread.start();
    }

    /**
     * Sets the gamestate in this Forwarder.
     * @param state GameState to be used.
     */
    protected static void setState(State state) {
        HostForwarder.state = state;
    }

    /**
     * Adds a Random Generator seed to this Forwarder.
     * @param seed the seed is set to this value.
     */
    public static void setSeed(RandomNumberGenerator seed) {
        HostForwarder.seed = seed;
    }

    /**
     * Waits on getting all the dicerolls from all the clients (this is only for one
     * client respectively). Involves some busy waiting. Passes the Exceptions over.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    void getRolls() throws IOException, InterruptedException, HashMismatchException{
        String hashStr = queuer.popRollHash();
        System.out.println("Got hash from " + ID);

        while (seed.getNumberSeedSources()>0) {
            Thread.sleep(10);
        }
        seed.addHash(ID, hashStr);

        String rollStr = queuer.popRollNumber();
        System.out.println("Got Number from " + ID);

        seed.addNumber(ID, rollStr);
        getrolls = false;
    }

    /**
     * Forwards and applies commands. Throws exception up one level.
     * @throws IOException
     */
    protected void playGame() throws IOException {

        while(true) {

            if (getrolls){
                try {
                    getRolls();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (HashMismatchException e) {
                    e.printStackTrace();
                }
            }
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

            Command reply = queuer.popCommand();

            checkAckCases(reply);

            if (state.winConditionsMet()) {
                running = false;
                break;
            }
            try {
                Thread.sleep(1); // Just sleep a bit to prevent busy looping
            } catch (InterruptedException e) {}
        }
    }

    /**
     * Check for acknowledgement, otherwise applies command to game state.
     * @param comm the command to be checked.
     */
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

    /**
     * Processes game commands and applies them to game state.
     * @param comm The command to be processed
     */
    private void processCommand(Command comm) {
        while (state.getCurrentPlayer().getID() != comm.getPlayer() && !(comm instanceof DefendCommand)){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //TODO HOTFIX check with bence. This is to prevent that commands are wrongly not verified if the next players
            //command is trying to execute before the current players command is executed. only an issue if commands are propagated
            //before they are applied, provoking teh clients to send their next command if its their turn
            //po26
        }
        Player currentPlayer = state.getCurrentPlayer();
        ArrayList<Action> playerActions = new ArrayList<>();
        if (comm instanceof AttackCommand){
            playerActions.add(processAttackCommand((AttackCommand) comm));
        }
        else if (comm instanceof DeployCommand){
            playerActions.addAll(processDeployCommand((DeployCommand) comm));
        }
        else if (comm instanceof FortifyCommand){
            playerActions.add(processFortifyCommand((FortifyCommand) comm));
        }
        else if (comm instanceof DrawCardCommand){
            playerActions.add(processDrawCardCommand((DrawCardCommand) comm));
        }
        else if (comm instanceof SetupCommand){
            playerActions.add(processSetupCommand((SetupCommand) comm));
        }
        else if (comm instanceof PlayCardsCommand){
            playerActions.addAll(processPlayCardsCommand((PlayCardsCommand) comm));
        }
        else if (comm instanceof DefendCommand){
            processDefendCommand((DefendCommand) comm);
        }
        else {
            System.out.println("Player " + ID + " cant process command " + comm.toJSONString());
            return;
        }

        if (playerActions.size() == 0 && !(comm instanceof DefendCommand)){
            System.out.println("End turn");
            playerActions.add(new FortifyAction(currentPlayer));
        }

        for(Action playerAction : playerActions) {
            if (playerAction.validateAgainstState(state)) {
                playerAction.performOnState(state);
            } else {
                System.out.println("Error move did not validate: " + comm);
                System.exit(1);
            }
        }

        if(state.winConditionsMet()){
            Player winner = state.getWinner();
            System.out.println("Winner is " + winner.getID());
            System.exit(0);
        }
        move_required = false;
        timer = System.currentTimeMillis();
    }

    /**
     * Processes a defend command against the game state.
     * @param comm The command to be processed
     */
    private void processDefendCommand(DefendCommand comm) {
        System.out.println("Processed Defend command by player " + ID);
        state.getPlayer(ID).getClient().pushCommand(comm);
    }

    /**
     * Processes the Play_cards command of the protocol and creates a list of relevant actions.
     * @param command The command to be parsed
     * @return List of actions the command implies.
     */
    private ArrayList<TradeAction> processPlayCardsCommand(PlayCardsCommand command){
        JSONObject payload = command.getPayload();
        int player = command.getPlayer();
        if (payload == null){
            ArrayList<TradeAction> acts = new ArrayList<>();
            acts.add(new TradeAction(state.getPlayer(player),null));
            return acts;
        }
        JSONArray cards = (JSONArray)(payload.get("cards"));


        ArrayList<ArrayList<RiskCard>> triplets = new ArrayList<>();
        for (Object tripletObject : cards) {
            JSONArray tripletJSON = (JSONArray) tripletObject;
            ArrayList<RiskCard> triplet = new ArrayList<>();
            for (Object aTripletJSON : tripletJSON) {
                int cardId = Integer.parseInt(aTripletJSON.toString());
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


    /**
     * Processes an Attack Command and retrieves the defend action from the defender as well.
     * @param command The attack command to be parsed
     * @return The AttackAction created from this command.
     */
    private AttackAction processAttackCommand(AttackCommand command){
        JSONArray attackPlan = command.getPayloadAsArray();
        int player = command.getPlayer();
        int originId = Integer.parseInt(attackPlan.get(0).toString());
        int objectiveId = Integer.parseInt(attackPlan.get(1).toString());
        int attackArmies = Integer.parseInt(attackPlan.get(2).toString());

        // Get rolls from all clients...
        // HostForwarder.setSeed(new RNGSeed(ListenerThread.getPlayers().size()));
        messageQueue.getRolls(ID);
        try {
            getRolls();
            while(seed.getNumberSeedSources()<state.getPlayers().size()) {
                Thread.sleep(10);
            }
            //while (!seed.isFinalised()) Thread.sleep(1);
            seed.finalise();
            seed.debug_printnums();


            DefendCommand def = queuer.popDefendCommand(0,0,0);

            int defendArmies = def.getPayloadAsInt();
            System.out.println("Defend armies: " + defendArmies);
            int[] attackDice = new int [attackArmies];
            for (int i = 0; i<attackArmies; i++){
                attackDice[i] = (int)(seed.nextInt() % 6 + 1);
                System.out.println(attackDice[i]);
            }

            int[] defendDice = new int [defendArmies];
            for (int i = 0; i<defendArmies; i++){
                defendDice[i] = (int)(seed.nextInt() % 6 + 1);
                System.out.println(defendDice[i]);
            }

            return new AttackAction(state.getPlayer(player),state.getCountryByID(originId),state.getCountryByID(objectiveId),attackDice,defendDice);
        } catch (InterruptedException | IOException | HashMismatchException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * Processes a fortification request
     * @param command the command to be parsed
     * @return Action to be taken by the player.
     */
    private FortifyAction processFortifyCommand(FortifyCommand command){
        if (command.get("payload") == null){
            return new FortifyAction(state.getPlayer(command.getPlayer()));
        }
        JSONArray fortification = command.getPayloadAsArray();
        int player = command.getPlayer();
        int originId = Integer.parseInt(fortification.get(0).toString());
        int objectiveId = Integer.parseInt(fortification.get(1).toString());
        int armies = Integer.parseInt(fortification.get(2).toString());

        return new FortifyAction(state.getPlayers().get(player),state.getCountryByID(originId),state.getCountryByID(objectiveId),armies);
    }

    /**
     * Processes the command for drawing a risk card.
     * @param command the command to be parsed
     * @return the action corresponding to the command.
     */
    private ObtainRiskCardAction processDrawCardCommand(DrawCardCommand command){
        int player = command.getPlayer();
        ObtainRiskCardAction act = new ObtainRiskCardAction(state.getPlayers().get(player));
        System.out.println("Interpreted draw command");
        return act;
    }

    /**
     * Processes an army deploy command
     * @param command the command to be processed
     * @return the list of actions corresponding to the command
     */
    private ArrayList<DeployArmyAction> processDeployCommand(DeployCommand command) {
        ArrayList<DeployArmyAction> deployArmyActions = new ArrayList<>();
        JSONArray territoryNumberPairs = command.getPayloadAsArray();
        int player = command.getPlayer();
        //construct list of deploy actions
        for (Object pair : territoryNumberPairs) {
            //extract parameters
            JSONArray pairArray = (JSONArray) pair;
            int countryId = Integer.parseInt(pairArray.get(0).toString());
            int armies = Integer.parseInt(pairArray.get(1).toString());

            deployArmyActions.add(new DeployArmyAction(state.getPlayer(player),state.getCountryByID(countryId),armies));
        }
        return deployArmyActions;
    }

    /**
     * Processes setup command at the beginning of the game.
     * @param command command to be processed
     * @return Relevant setup action.
     */
    private SetupAction processSetupCommand(SetupCommand command){
        int countryId = command.getPayloadAsInt();
        return new SetupAction(state.getPlayer(command.getPlayer()),state.getCountryByID(countryId));
    }

    /**
     * Signals this thread that an acknowledgement is required by this client.
     * @param ack_id The ack_id of the acknowledgement expected.
     */
    protected void signalAck(int ack_id) {
        last_ack = ack_id;
        timer = System.currentTimeMillis();
        ack_received = false;
    }

    protected boolean hasSeed() {
        return seed != null;
    }

    public void getRollsLater(){
        getrolls = true;
    }

    @Override
    public void run() {
        //TODO potentially dirty hotfix, see commit message
        while(running){
            try {
                if (input.ready()) {
                    Command reply = Command.parseCommand(input.readLine());
                    if (reply instanceof RollHashCommand){
                        queuer.pushRollHash(reply.getPayloadAsString().toString());
                    }
                    else if (reply instanceof RollNumberCommand){

                        queuer.pushRollNumber(reply.getPayloadAsString().toString());
                    }


                    else {
                        queuer.pushCommand(reply);
                    }
                    System.out.println("in Player " + ID + ": " + reply);
                    messageQueue.sendAll(reply, ID);
                }
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
