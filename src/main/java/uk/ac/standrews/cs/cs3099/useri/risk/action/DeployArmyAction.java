package uk.ac.standrews.cs.cs3099.useri.risk.action;

import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.game.TurnStage;

/**
 * Deploying armies at the beginning of the turn. This is compulsory!
 * Created by bs44 on 30/01/15.
 */
public class DeployArmyAction extends Action{
    private final int armies;
    private final Country country;

    public DeployArmyAction(Player player, Country country, int armies) {
        super(player);
        this.armies = armies;
        this.country = country;
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
        if (this.getPlayer().equals(state.getCurrentPlayer())) {
            if (state.getTurnStage() == TurnStage.STAGE_DEPLOYING) {
                if (this.country.getOwner().equals(this.getPlayer())) {
                    if (this.getPlayer().getUnassignedArmy() >= this.armies) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * increases number of armies on the specified country.
     *
     * @param state
     */
    @Override
    public void performOnState(State state) {
        country.setTroops(country.getTroops() + armies);
        //TODO is this all that needs to be done or do we send the messages from here?
    }
}
