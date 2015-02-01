package uk.ac.standrews.cs.cs3099.useri.risk.action;

import uk.ac.standrews.cs.cs3099.useri.risk.game.State;

/**
 * Getting risk cards. This is compulsory. If you won an attack, you get a risk card.
 * Created by bs44 on 30/01/15.
 */
public class ObtainRiskCardAction extends Action {
    /**
     * Validates whether the action can be made against the current game state.
     * No Action is performed.
     *
     * @param state
     * @return true if the player calling the action is the one that can make the turn, and has already won a battle.
     * false if there is an error
     */
    @Override
    public boolean validateAgainstState(State state) {
        if ( getPlayer().equals( state.getCurrentPlayer() ) ) {
            if (state.wonBattle()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Performs the action on the game state, alters it accordingly returning the new state
     *
     * @param state
     */
    @Override
    public void performOnState(State state) {
        getPlayer().addCard(state.getCard());
        state.nextAction();
    }
}
