package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.ac.standrews.cs.cs3099.useri.risk.action.*;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.ClientSocketHandler;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.HashMismatchException;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.RandomNumberGenerator;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.*;

import java.io.IOException;
import java.util.ArrayList;

/**
 * runs the main game loop and gets turns from the players
 *
 */
public class GameEngine implements Runnable{
    /**
     * This method runs the main game loop
     * Simple outline:
     * 1. polls an action from the active client
     * 2. verify the action against the current game state
     * 3. execute action
     * 4. checks win conditions
     * 5. sends out update notification to all clients
     *
     */
    private State state;
    private ClientSocketHandler csh;

    /**
     * GameEngine constructor which takes clientsockethandler
     * @param csh
     */
    public GameEngine(ClientSocketHandler csh){
        this.csh = csh;
    }

    /**
     * Empty GameEngine Constructor
     */
    public GameEngine() {
    }

    @Override
    public void run(){
        initialise();
        gameLoop();
    }

    public void initialise(State state, ArrayList<Client> clients) {
        this.state = state;
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
    void initialise(){
        //create gamestate
        State gamestate = new State();
        state = gamestate;


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
        ArrayList<Player> players = new ArrayList<Player>();

        for (Client c : csh.getAllClients()){
            players.add(new Player(c.getPlayerId(),c, c.getPlayerName()));
        }
        gamestate.setup(map,players);


        //Now roll dice to determine first player
        int firstPlayer = csh.determineFirstPlayer();

        System.out.println(firstPlayer);
        System.out.println("Player " + gamestate.getPlayer(firstPlayer).getName() + " goes first!");
        gamestate.setFirstPlayer(gamestate.getPlayer(firstPlayer));
        gamestate.setCurrentPlayer(firstPlayer);

        //now shuffle cards

        System.out.println("shuffling cards");

        gamestate.shuffleRiskCards(csh.popSeed());

        System.out.println("shuffled cards");

        //setup the countries in the normal game loop

        csh.linkGameState(state);

    }

    /**
     * Main loops to run game
     */
    void gameLoop(){
		System.out.println("Game Loop running...");
        Player currentPlayer;
        while(true) {
            currentPlayer = state.getCurrentPlayer();
            System.out.println("Ask player " + currentPlayer.getID() + " (" + currentPlayer.getName() + ")");
            Command currentCommand = currentPlayer.getClient().popCommand();
            //if its local, propagate
            if (csh != null && currentPlayer.getClient().isLocal()){
               csh.sendCommand(currentCommand);
            }
            processCommand(currentPlayer, currentCommand);

            if (state.winConditionsMet()) break;

        }
	}

    private void processCommand(Player currentPlayer, Command currentCommand) {
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
            System.exit(0);
        }
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
        if (payload == null){
            ArrayList<TradeAction> acts = new ArrayList<>();
            acts.add(new TradeAction(state.getPlayer(player),null));
            return acts;
        }

        JSONArray cards = (JSONArray)(payload.get("cards"));

        ArrayList<ArrayList<RiskCard>> triplets = new ArrayList<ArrayList<RiskCard>>();
        for (Object tripletObject : cards) {
            JSONArray tripletJSON = (JSONArray) tripletObject;
            ArrayList<RiskCard> triplet = new ArrayList<RiskCard>();
            for (Object aTripletJSON : tripletJSON) {
                int cardId = Integer.parseInt(aTripletJSON.toString());
                triplet.add(state.getPlayer(player).getRiskCardById(cardId));
            }
            triplets.add(triplet);
        }

        ArrayList<TradeAction> acts = new ArrayList<>();
        for (ArrayList<RiskCard> triplet : triplets){
            TradeAction act = new TradeAction(state.getPlayer(player),triplet);

            System.out.println("Interpreted trade command");
            acts.add(act);
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

        // Get rolls from all clients...
        // HostForwarder.setSeed(new RNGSeed(ListenerThread.getPlayers().size()));

        try {
            RandomNumberGenerator seed = csh.popSeed();
            //while (!seed.isFinalised()) Thread.sleep(1);
            seed.finalise();

            DefendCommand def = state.getCountryByID(objectiveId).getOwner().getClient().popDefendCommand(originId, objectiveId, attackArmies);
            //if its local, propagate
            if (csh != null && state.getCountryByID(objectiveId).getOwner().getClient().isLocal()){
                csh.sendCommand(def);
            }
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
        } catch (HashMismatchException e) {
            e.printStackTrace();
        }
        return null;
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

        FortifyAction act = new FortifyAction(state.getPlayer(player),state.getCountryByID(originId),state.getCountryByID(objectiveId),armies);
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
        ObtainRiskCardAction act = new ObtainRiskCardAction(state.getPlayer(player));
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

            //create deploy action
            deployArmyActions.add(new DeployArmyAction(state.getPlayer(player),state.getCountryByID(countryId),armies));
//
//            //push to client
//
//
//
//            //pushed action to client
//
//            System.out.println("pushed deploy action to client " + player);


        }

        //TODO ret multiple

        return deployArmyActions;

    }

    private SetupAction processSetupCommand(SetupCommand command){
        int countryId = command.getPayloadAsInt();
        return  new SetupAction(state.getPlayer(command.getPlayer()),state.getCountryByID(countryId));
    }




}
