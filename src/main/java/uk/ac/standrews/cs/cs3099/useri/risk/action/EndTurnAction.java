package uk.ac.standrews.cs.cs3099.useri.risk.action;

import uk.ac.standrews.cs.cs3099.useri.risk.game.State;

/**
 * Created by bs44 on 30/01/15.
 */
public class EndTurnAction extends Action {
    /**
     * Validates whether the action can be made against the current game state.
     * This action only allowed when our player is the turnPlayer.
     *
     * @param state
     * @return true if turnPlayer equals this.Player.
     * false otherwise.
     */
    @Override
    public boolean validateAgainstState(State state) {
        return this.getPlayer().equals(state.getCurrentPlayer());
    }

    /**
     * Performs the action on the game state, alters it accordingly returning the new state
     *
     * @param state
     */
    @Override
    public void performOnState(State state) {
        state.endTurn();
        return;
    }
}
