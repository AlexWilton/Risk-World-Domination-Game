package uk.ac.standrews.cs.cs3099.useri.risk.clients;

import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.RandomNumberGenerator;

/**
 * Client that communicates over the Network only.
 */
public class NetworkClient extends Client {

    private boolean ready;

    @Override
    public void newSeedComponent() {
    }

    public NetworkClient(State gamestate, RandomNumberGenerator rng){
        super(gamestate, rng);
        ready = false;
        this.gameState = gamestate;
    }

    @Override
    protected byte[] getSeedComponent(){
        return null;
    }

    @Override
    public int getDefenders(Country attacker, Country objective, int attackingArmies) {
        return 0;
    }

    @Override
    public boolean isReady(){
        return ready;
    }

    public boolean isLocal(){
        return false;
    }


}
