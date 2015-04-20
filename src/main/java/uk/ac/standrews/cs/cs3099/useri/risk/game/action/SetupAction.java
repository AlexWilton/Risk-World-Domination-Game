package uk.ac.standrews.cs.cs3099.useri.risk.game.action;

import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;

/**
 * seting up countries
 * Created by bs44 on 30/01/15.
 */
public class SetupAction extends Action {


    private Country country;
    private Player player;

    public SetupAction (Player player, Country country) {
        super(player);
        this.country = country;
        this.player = player;

    }


    /**
     * Validates this action against the game state. For validation, the action's owver has to be correct, and the
     * country has to be empty, plus the player has to hava at least as many armies as they want to place (1).
     *
     * @param state
     * @return true if it is valid
     * false if there is an error
     */
    @Override
    public boolean validateAgainstState(State state) {
        //must be player's turn
        if(player != state.getCurrentPlayer()){
            System.err.println("Not current player");
            return false;
        }

        //treat situation where all countries claimed separately
        if(state.hasUnassignedCountries()){
            if (country.getOwner() != null)
                return false;
        }else{
            //player must own country
            if(country.getOwner() != player)
                return false;

            //player must have at least one unassigned army
            if(player.getUnassignedArmies() < 1)
                return false;
        }

        return true;
    }


    /**
     *
     * @param state The state to be changed
     */
    @Override
    public void performOnState(State state) {

        //treat situation where all countries claimed separately
        if(state.hasUnassignedCountries()) { //claiming unclaimed country:
            country.setOwner(player);
            player.addCountry(country);

            //player must put one army in a country when claiming it
            country.setTroops(1);
            player.setUnassignedArmies(player.getUnassignedArmies() - 1);
        } else { //reinforcing already claimed country with a single army
            country.setTroops(country.getTroops() + 1);
            player.setUnassignedArmies( player.getUnassignedArmies() - 1);
        }

        //check for end of setup
        boolean armyLeftToAssign = false;
        if (!state.anyoneHasUnassignedArmies()) {
            state.endPreGame();
        }else{
            //skip players until player with armies to deploy is left
            do{
                state.nextPlayer();
            }while(state.getCurrentPlayer().getUnassignedArmies() == 0);
        }

    }


}