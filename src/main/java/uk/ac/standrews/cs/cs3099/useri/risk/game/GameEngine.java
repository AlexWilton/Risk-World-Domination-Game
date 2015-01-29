package uk.ac.standrews.cs.cs3099.useri.risk.game;

import java.util.ArrayList;

import uk.ac.standrews.cs.cs3099.useri.risk.action.Action;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;

/**
 * runs the main game loop and gets turns from the players
 *
 */
public class GameEngine {
	private State state;
	private ArrayList<Client> clients;
	
	
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


	public void initialise(State state, ArrayList<Client> clients){
        this.state = state;
        this.clients = clients;
    }
}
