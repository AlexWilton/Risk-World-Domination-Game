package uk.ac.standrews.cs.cs3099.useri.risk.action;

import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.game.TurnStage;

import java.util.Arrays;

/**
 * seting up countries
 * Created by bs44 on 30/01/15.
 */
public class SetupAction extends Action {


    private Country country;
    private Player player;

    public SetupAction (Player player, Country country) {
        super(player, TurnStage.STAGE_TRADING);
        this.country = country;
        this.player = player;

    }


    /**
     *
     *
     * @param state
     * @return true if it is valid
     * false if there is an error
     */
    @Override
    public boolean validateAgainstState(State state) {
        if(!state.hasUnassignedCountries())
            return false;

        if(player != state.getCurrentPlayer())
            return false;

        if (country.getOwner() != null)
            return false;

        return true;
    }


    /**
     *
     *
     * @param state The state to be changed
     */
    @Override
    public void performOnState(State state) {
        country.setOwner(player);
        state.endTurn();
        System.out.println (player.getName() + " took possession of " + country.getCountryName());
    }


}