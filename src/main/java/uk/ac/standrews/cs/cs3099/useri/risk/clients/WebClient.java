package uk.ac.standrews.cs.cs3099.useri.risk.clients;


import uk.ac.standrews.cs.cs3099.useri.risk.action.Action;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;

public class WebClient extends Client {
    /**
     * @return the next action this player takes based on current game state
     */
    @Override
    public Action getAction() {
        return null;
    }

    /**
     * notify player of the
     */
    @Override
    public void pushGameState() {

    }

    @Override
    public int getDefenders(Country attackingCountry, Country defendingCountry, int attackingArmies) {
        //Auto defend with max troops possible
        return (defendingCountry.getTroops() > 1 ? 2 : 1);
    }

    @Override
    public int[] getSeedComponent() {
        return new int[0];
    }
}
