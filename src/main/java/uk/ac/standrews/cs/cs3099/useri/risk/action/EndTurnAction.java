package uk.ac.standrews.cs.cs3099.useri.risk.action;

import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.game.TurnStage;

/**
 * Created by bs44 on 30/01/15.
 */
//TODO is this action even needed?
public class EndTurnAction extends Action {
    public EndTurnAction(Player player) {
        super(player, TurnStage.STAGE_FINISH);
    }

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
