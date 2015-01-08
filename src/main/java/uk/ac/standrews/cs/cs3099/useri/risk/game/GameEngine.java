package uk.ac.standrews.cs.cs3099.useri.risk.game;

import java.util.ArrayList;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;

/**
 * runs the main game loop and gets turns from the players
 *
 */
public class GameEngine {
	private State gameState;
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
		
	}
	public void initialise(State state, ArrayList<Client> clients){}
}
