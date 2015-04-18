package uk.ac.standrews.cs.cs3099.useri.risk.action;

import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;

/**
 * This action is performed when the previous AttackAction has been successful. In this case, the player can decide
 * how many armies it wants to move to the captured country.
 */
public class AttackCaptureAction  extends Action {
    int origin;
    int destination;
    int armies;

    public AttackCaptureAction(Player player, int originID, int destID, int armies) {
        super(player);
        origin = originID;
        destination = destID;
        this.armies = armies;
    }

    /**
     * Validates this move against the state. For this action to be valid, both the origin and destination countries
     * have to be owned by the specified player, and the number of armies in the origin country has to be at least 1
     * greater than the number of armies we want to move to the captured country.
     */
    public boolean validateAgainstState(State state) {
        if (!super.validateAgainstState(state))
            return false;
        if (!state.wonBattle())
            return false;
        if (!state.getCountryByID(origin).getOwner().equals(player))
            return false;
        if (!state.getCountryByID(destination).getOwner().equals(player))
            return false;
        if (state.getCountryByID(origin).getTroops() <= armies)
            return false;
        if(!state.isAttackCaptureNeeded())
            return false;

        return true;
    }

    @Override
    public void performOnState(State state) {
        Country originCountry = state.getCountryByID(origin);
        Country destinationCountry = state.getCountryByID(destination);

        originCountry.setTroops(originCountry.getTroops() - armies);
        destinationCountry.setTroops(armies);

        state.markAttackCaptureNotNeeded();
    }
}
