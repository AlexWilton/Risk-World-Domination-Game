package uk.ac.standrews.cs.cs3099.useri.risk.action;

import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.game.TurnStage;

/**
 * abstract Action class
 *
 * turns consist of multiple Actions
 *
 * actions are constructed by Clients, and validate themselves against a game state, and can produce a new state themselves.
 *
 */
public abstract class Action {
    protected Player player;
    protected TurnStage stage;

    public Action(Player player, TurnStage stage){
        this.player = player;
        this.stage = stage;
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
	public boolean validateAgainstState(State state){
        if (player.equals(state.getCurrentPlayer())) {
            if (state.getTurnStage().equals(stage)) {
                return true;
            }
        }
        return false;
    }

	/**
	 * Performs the action on the game state, alters it accordingly returning the new state
	 */
	public abstract void performOnState(State state);

    public Player getPlayer() {
        return player;
    }
}
