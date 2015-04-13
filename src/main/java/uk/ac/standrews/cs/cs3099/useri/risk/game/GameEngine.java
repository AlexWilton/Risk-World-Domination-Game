package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.ac.standrews.cs.cs3099.risk.game.RandomNumbers;
import uk.ac.standrews.cs.cs3099.useri.risk.action.*;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.RNGSeed;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.ClientSocketHandler;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.*;

import java.util.ArrayList;

/**
 * runs the main game loop and gets turns from the players
 *
 */
public class GameEngine implements Runnable{
	private State state;
	
	
	/**
	 * This method runs the main game loop
	 * 
	 * 1. polls an action from the active client
	 * 2. verify the action against the current game state
	 * 3. execute action
	 * 4. checks win conditions
	 * 5. sends out update notification to all clients
	 * 
	 */

    private ClientSocketHandler csh;

    public GameEngine(ClientSocketHandler csh){
        this.csh = csh;
    }

    @Override
    public void run(){
        initialise();
        gameLoop();
    }

	public void gameLoop(){
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
                playerActions.add(processPlayCardsCommand((PlayCardsCommand) currentCommand));
            }
            else {
                System.out.println("cant process command " + currentCommand.toJSONString());
                continue;
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

        }
	}


	public void initialise(State state){
        this.state = state;
    }


    public void initialise(State state, ArrayList<Client> clients){
        this.state=state;
    }



    public void initialise(){
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


    private TradeAction processPlayCardsCommand(PlayCardsCommand command){
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

        for (ArrayList<RiskCard> triplet : triplets){
            TradeAction act = new TradeAction(state.getPlayers().get(player),triplet);

            System.out.println("Interpreted trade command");
            return act;
        }


        //TODO ret multiple
        return null;

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

        DefendCommand def = state.getCountryByID(objectiveId).getOwner().getClient().popDefendCommand(originId,objectiveId,attackArmies);
        //if its local, propagate
        if (state.getCountryByID(objectiveId).getOwner().getClient().isLocal()){
            csh.sendCommand(def);
        }

        int defendArmies = def.getPayloadAsInt();

        RNGSeed seed = csh.popSeed();

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

            //create deploy action
            deployArmyActions.add(new DeployArmyAction(state.getPlayers().get(player),state.getCountryByID(countryId),armies));
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
        SetupAction act = new SetupAction(state.getPlayer(command.getPlayer()),state.getCountryByID(countryId));
        return act;

    }




}
