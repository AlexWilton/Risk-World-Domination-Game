package uk.ac.standrews.cs.cs3099.useri.risk.action;

import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;

/**
 * Deploying armies at the beginning of the turn. This is compulsory!
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
        if (super.validateAgainstState(state)) {
            if (country.getOwner().equals(player)) {

                if (player.getUnassignedArmies() >= armies) {
                    return true;
                }
                else System.err.println("not enough armies, got " + player.getUnassignedArmies() + ", wanted " + armies);
            }
            else System.err.println("owner not valid");
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
        player.setUnassignedArmies(getPlayer().getUnassignedArmies() - armies);

        if(state.getCurrentPlayer().getUnassignedArmies() == 0)
            state.nextStage();
    }
}
