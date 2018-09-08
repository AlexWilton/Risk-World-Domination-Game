package uk.ac.standrews.cs.cs3099.useri.risk.game.action;

import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.CountrySet;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Player;
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
        if (!super.validateAgainstState(state))
            return false;

        if(!country.getOwner().equals(player))
            return false;

        if (player.getUnassignedArmies() < armies)
            return false;

        // if this is a deployment leaving less than 2 armies,
        // ensure that there are no countries requiring deployment which haven't been deployed to
        if(player.getUnassignedArmies() - armies < 2) {
            CountrySet countryWhereAtLeastOneRequiresTwoArmies = state.getSetOfCountriesWhereAtLeastOneNeedsToBeDeployedTo();
            if (countryWhereAtLeastOneRequiresTwoArmies != null) {
                boolean requiredDeploymentTakesPlace = false;
                for (Country aRequiredCountry : countryWhereAtLeastOneRequiresTwoArmies) {
                    if (aRequiredCountry.getCountryId() == country.getCountryId())
                        requiredDeploymentTakesPlace = true;
                }
                if(!requiredDeploymentTakesPlace)
                    return false;
            }
        }

        return true;
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

        //if a required deployment is made, remove requirement of all needed deployments
        CountrySet countryWhereAtLeastOneRequiresTwoArmies = state.getSetOfCountriesWhereAtLeastOneNeedsToBeDeployedTo();
        if(countryWhereAtLeastOneRequiresTwoArmies != null){
            if(armies >= 2){
                for(Country requiredCountry : countryWhereAtLeastOneRequiresTwoArmies){
                    if(requiredCountry == country)
                        state.setSetOfCountriesWhereAtLeastOneNeedsToBeDeployedTo(null);
                }
            }
        }

        if(state.getCurrentPlayer().getUnassignedArmies() == 0)
            state.nextStage();
    }
}
