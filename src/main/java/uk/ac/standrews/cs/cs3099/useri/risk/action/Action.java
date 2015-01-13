package uk.ac.standrews.cs.cs3099.useri.risk.action;

import uk.ac.standrews.cs.cs3099.useri.risk.game.State;

/**
 * abstract Action class
 * 
 * turns consist of multiple Actions
 * 
 * actions are constructed by Clients, and validate themselves against a game state, and can produce a new gamestate
 *
 */
public abstract class Action {

	private State currentState;
	
	
	/**
	 * Validates the action against the current game state, which is not altered
	 * @return
	 * 1 if it is valid
	 * !=1 if there is an error, see below for error codes
	 */
	public abstract int validate();
	
	/**
	 * Performs the action on the game state and alters it accordingly. Call validate first (merge?)
	 */
	public abstract void perform();
}
