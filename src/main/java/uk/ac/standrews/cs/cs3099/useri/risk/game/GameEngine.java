package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.ac.standrews.cs.cs3099.useri.risk.game.action.*;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Map;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.RiskCard;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.ClientSocketHandler;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.HashMismatchException;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.RandomNumberGenerator;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.*;

import java.util.ArrayList;

/**
 * runs the main game loop and gets turns from the players
 *
 */
public class GameEngine implements Runnable{

    private State state;
    private ClientSocketHandler csh;

    /**
     * GameEngine constructor which takes clientsockethandler
     * @param csh The ClientSocketHandler that this engine should call.
     */
    public GameEngine(ClientSocketHandler csh){
        this.csh = csh;
    }

    @Override
    public void run(){
        initialise();
        gameLoop();
    }

    /**
     * initialises game state with given state
     * @param state : game's state object
     */
    public void initialise(State state){
        this.state = state;
    }

    /**
     * Constructs a game state and stored in GameEngine object
     */
    public void initialise(){
        //create game state
        State gameState = new State();
        state = gameState;

        //initialise map
        Map map = new Map();

        //wait till we are "playing"
        while (csh.getProtocolState() != ClientSocketHandler.ProtocolState.RUNNING){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //setup the players
        ArrayList<Player> players = new ArrayList<>();

        for (Client c : csh.getAllClients()){
            players.add(new Player(c.getPlayerId(),c, c.getPlayerName()));
        }
        gameState.setup(map,players);

        //Now roll dice to determine first player
        int firstPlayer = csh.determineFirstPlayer();

        System.out.println(firstPlayer);
        System.out.println("Player " + gameState.getPlayer(firstPlayer).getName() + " goes first!");
        gameState.setFirstPlayer(gameState.getPlayer(firstPlayer));
        gameState.setCurrentPlayer(firstPlayer);

        //now shuffle cards
        System.out.println("shuffling cards");
        gameState.shuffleRiskCards(csh.popSeed());
        System.out.println("shuffled cards");

        //setup the countries in the normal game loop
        csh.linkGameState(state);
    }

    /**
     * This method runs the main game loop
     * Simple outline:
     * 1. polls an action from the active client
     * 2. verify the action against the current game state
     * 3. execute action
     * 4. checks win conditions
     * 5. sends out update notification to all clients
     */
    public void gameLoop(){
        Player currentPlayer;
        while(true) {
            currentPlayer = state.getCurrentPlayer();
            Command currentCommand = currentPlayer.getClient().popCommand();

            sendIfLocal(csh, currentPlayer,currentCommand);

            processCommand(currentPlayer, currentCommand);
            if (checkIfPlayerLost() || state.winConditionsMet()) break;
        }
    }

    public void sendIfLocal(ClientSocketHandler csh, Player o, Command currentCommand){
        if(csh != null && o.getClient().isLocal()){
            csh.sendCommand(currentCommand);
        }
    }

    private boolean checkIfPlayerLost(){

        int playerRemoved = state.getRemovedPlayer();

        if (playerRemoved >= 0){
            csh.removePlayer(playerRemoved);
            if (playerRemoved == csh.getLocalClientId())
                return true;
        }
        return false;
    }

    /**
     * Processes game commands and applies them to game state.
     * @param currentCommand The command to be processed
     * @param currentPlayer The player to make the move.
     */
    private void processCommand(Player currentPlayer, Command currentCommand) {
        ArrayList<Action> playerActions = new ArrayList<>();
        if (currentCommand instanceof AttackCommand){
            playerActions.add(processAttackCommand((AttackCommand) currentCommand));
        }
        else if (currentCommand instanceof AttackCaptureCommand) {
            playerActions.add(processAttackCaptureCommand((AttackCaptureCommand) currentCommand));
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
            playerActions.add(new FortifyAction(currentPlayer));
        }

        for(Action playerAction : playerActions) {
            if (playerAction.validateAgainstState(state)) {
                playerAction.performOnState(state);
            } else {
                System.out.println("Error move did not validate: " + currentCommand);
            }
        }

        if(state.winConditionsMet()) {
            Player winner = state.getWinner();
            System.out.println("Winner is " + winner.getID());
        }
    }

    /**
     * Processes an attack capture command without any sort of validation against the gameState.
     * @param comm The command to be processed
     * @return Action corresponding to the command.
     */
    private Action processAttackCaptureCommand(AttackCaptureCommand comm) {
        int origin = comm.getOrigin();
        int destination = comm.getDestination();
        int playerID = comm.getPlayer();
        int armies = comm.getArmies();

        Player player = state.getPlayer(playerID);
        return new AttackCaptureAction(player, origin, destination, armies);
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
                triplet.add(state.getPlayer(player).getRiskCardById(cardId));
            }
            triplets.add(triplet);
        }

        ArrayList<TradeAction> acts = new ArrayList<>();
        for (ArrayList<RiskCard> triplet : triplets){
            TradeAction act = new TradeAction(state.getPlayer(player),triplet);

            acts.add(act);
        }
        return acts;
    }

    /**
     * Processes an Attack Command and retrieves the defend action from the defender as well.
     * @param command The attack command to be parsed
     * @return The AttackAction created from this command.
     */
    private AttackAction processAttackCommand(AttackCommand command) {
        JSONArray attackPlan = command.getPayloadAsArray();
        int player = command.getPlayer();
        int originId = Integer.parseInt(attackPlan.get(0).toString());
        int objectiveId = Integer.parseInt(attackPlan.get(1).toString());
        int attackArmies = Integer.parseInt(attackPlan.get(2).toString());

        try {

            Player defender = state.getCountryByID(objectiveId).getOwner();
            DefendCommand def = defender.getClient().popDefendCommand(originId, objectiveId, attackArmies);

            sendIfLocal(csh, defender, def);

            RandomNumberGenerator seed = csh.popSeed();
            seed.finalise();

            int defendArmies = def.getPayloadAsInt();

            int[] attackDice = makeDice(seed, attackArmies);
            int[] defendDice = makeDice(seed, defendArmies);

            return new AttackAction(state.getPlayer(player),state.getCountryByID(originId),state.getCountryByID(objectiveId),attackDice,defendDice);
        } catch (HashMismatchException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int[] makeDice(RandomNumberGenerator seed, int size){
        int[] dice = new int [size];
        for(int i = 0; i<size; i++){
            dice[i] = (int)(seed.nextInt() % 6 + 1);
            //System.out.println(dice[i]);
        }
        return dice;
    }
    /**
     * Processes a fortification request
     * @param command the command to be parsed
     * @return Action to be taken by the player.
     */
    private FortifyAction processFortifyCommand(FortifyCommand command) {
        if (command.get("payload") == null){
            return new FortifyAction(state.getPlayer(command.getPlayer()));
        }
        JSONArray fortification = command.getPayloadAsArray();
        int player = command.getPlayer();
        int originId = Integer.parseInt(fortification.get(0).toString());
        int objectiveId = Integer.parseInt(fortification.get(1).toString());
        int armies = Integer.parseInt(fortification.get(2).toString());

        FortifyAction act = new FortifyAction(state.getPlayer(player),state.getCountryByID(originId),state.getCountryByID(objectiveId),armies);

        return act;
    }

    /**
     * Processes the command for drawing a risk card.
     * @param command the command to be parsed
     * @return the action corresponding to the command.
     */
    private ObtainRiskCardAction processDrawCardCommand(DrawCardCommand command) {
        int player = command.getPlayer();
        ObtainRiskCardAction act = new ObtainRiskCardAction(state.getPlayer(player));
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
        //construct amount of deploy actions
        for (Object pair : territoryNumberPairs) {
            //extract parameters
            JSONArray pairArray = (JSONArray) pair;
            int countryId = Integer.parseInt(pairArray.get(0).toString());
            int armies = Integer.parseInt(pairArray.get(1).toString());
            //create deploy action
            deployArmyActions.add(new DeployArmyAction(state.getPlayer(player),state.getCountryByID(countryId),armies));
        }
        return deployArmyActions;
    }

    /**
     * Processes setup command at the beginning of the game.
     * @param command command to be processed
     * @return Relevant setup action.
     */
    private SetupAction processSetupCommand(SetupCommand command) {
        int countryId = command.getPayloadAsInt();
        return  new SetupAction(state.getPlayer(command.getPlayer()),state.getCountryByID(countryId));
    }
}
