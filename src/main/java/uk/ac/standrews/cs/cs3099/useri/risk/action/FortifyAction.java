package uk.ac.standrews.cs.cs3099.useri.risk.action;

import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.game.TurnStage;

/**
 * Voluntary
 * Created by bs44 on 30/01/15.
 */
public class FortifyAction extends Action {
    private final Country from;
    private final Country to;
    private final int armies;

    public FortifyAction(Player player, Country from, Country to, int armies) {
        super(player);
        this.from = from;
        this.to = to;
        this.armies = armies;
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
        if (player.equals(state.getCurrentPlayer())) {
            if (state.getTurnStage().equals(TurnStage.STAGE_FORTIFY)) {
                if (from.getOwner().equals(to.getOwner())) {
                    if (from.getTroops() > armies) {
                        return true;
                    }
                }
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
        from.setTroops(from.getTroops() - armies);
        to.setTroops(to.getTroops() + armies);
        state.nextAction();
    }
}
