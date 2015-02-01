package uk.ac.standrews.cs.cs3099.useri.risk.action;

import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;

/**
 * Attacking players. This is optional.
 * Created by bs44 on 30/01/15.
 */
public class AttackAction extends Action{
    Country from;
    Country to;

    public AttackAction (Player player, Country from, Country to, int attack, int defend){
        super(player);
        this.from = from;
        this.to = to;
        //TODO do we need the numbers of armies attacking / defending as well?
    }


    /**
     * Validates whether the action can be made against the current game state.
     * No Action is performed.
     *
     * @param state
     * @return true if it is valid
     * false if there is an error
     */
    @Override
    public boolean validateAgainstState(State state) {
        return false;
    }

    /**
     * Performs the action on the game state, alters it accordingly returning the new state
     *
     * @param state
     */
    @Override
    public void performOnState(State state) {
        state.nextAction();
    }
}
