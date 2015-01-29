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

	/**
	 * Validates whether the action can be made against the current game state.
     * No Action is performed.
	 * @return
	 * true if it is valid
	 * false if there is an error
	 */
	public abstract boolean validateAgainstState(State state);
	
	/**
	 * Performs the action on the game state, alters it accordingly returning the new state
	 */
	public abstract State performOnState(State state);
}
