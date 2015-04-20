package uk.ac.standrews.cs.cs3099.useri.risk.game.action;

import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;

/**
 * abstract Action class
 *
 * turns consist of multiple Actions
 *
 * actions are constructed by Clients, and validate themselves against a game state, and can produce a new state themselves.
 *
 */
public abstract class Action {
    Player player;


    Action(Player player){
        this.player = player;

    }
	/**
	 * Validates whether the action can be made against the current game state.
     * No Action is performed.
     * All actions need to be taken at the stage they correspond to and by the player whi needs to take their turn. This
     * is universal across all actions, so contained in this abstract class.
	 * @return
	 * true if it is valid
	 * false if there is an error
	 */
	public boolean validateAgainstState(State state) {
        return (player.getID() == state.getCurrentPlayer().getID());
    }

	/**
	 * Performs the action on the game state, alters it accordingly returning the new state
	 */
	public abstract void performOnState(State state);

    Player getPlayer() {
        return player;
    }
}
