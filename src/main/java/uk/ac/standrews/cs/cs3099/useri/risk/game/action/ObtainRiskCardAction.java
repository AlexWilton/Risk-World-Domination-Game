package uk.ac.standrews.cs.cs3099.useri.risk.game.action;

import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.RiskCard;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.game.TurnStage;

/**
 * Getting risk cards. This is compulsory. If you won an attack, you get a risk card.
 * Created by bs44 on 30/01/15.
 */
public class ObtainRiskCardAction extends Action {
    public ObtainRiskCardAction(Player player) {
        super(player);
    }

    /**
     * Validates whether the action can be made against the current game state.
     * No Action is performed.
     *
     * @param state the current game state
     * @return true if the player calling the action is the one that can make the turn, and has already won a battle.
     * false if there is an error
     */
    @Override
    public boolean validateAgainstState(State state) {
        if ( super.validateAgainstState(state) ) {
            if (state.wonBattle()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Performs the action on the game state, alters it accordingly.
     *
     * @param state the current game state to change
     */
    @Override
    public void performOnState(State state) {
        RiskCard c = state.getCard();
        if (c != null)
            player.addCard(state.getCard());
        state.setTurnStage(TurnStage.STAGE_FORTIFY);
    }
}
