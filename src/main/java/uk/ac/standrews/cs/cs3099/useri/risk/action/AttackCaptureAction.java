package uk.ac.standrews.cs.cs3099.useri.risk.action;

import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;

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
