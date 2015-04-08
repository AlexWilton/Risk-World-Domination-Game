package uk.ac.standrews.cs.cs3099.useri.risk.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import uk.ac.standrews.cs.cs3099.useri.risk.action.Action;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.CLIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.ClientSocketHandler;

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
            Action playerAction = currentPlayer.getPlayerAction();
            if (playerAction.validateAgainstState(state)) {
                playerAction.performOnState(state);
            }else{
                System.out.println("Error move did not validate");
                System.exit(1);
            }

            if(state.winConditionsMet()){
                Player winner = state.getWinner();

                System.out.println("Winner is " + winner.getID());
                //TODO follow endGame protocol
                System.exit(0);
            }

            //TODO send out update notifications to all clients

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

        //now shuffle cards

        System.out.println("shuffling cards");

        gamestate.shuffleRiskCards(csh.popSeed());

        System.out.println("shuffled cards");

        //setup the countries in the normal game loop

        state = gamestate;

    }
}
