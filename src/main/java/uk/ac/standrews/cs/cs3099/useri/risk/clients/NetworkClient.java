package uk.ac.standrews.cs.cs3099.useri.risk.clients;

import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;


public class NetworkClient extends Client {

    private boolean ready;

    @Override
    public void newSeedComponent() {
    }

    public NetworkClient(State gamestate){
        super(gamestate);
        ready = false;
        this.gameState = gamestate;
    }

    @Override
    public void pushGameState(){ }

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

    public void setReady (boolean set){
        ready = set;
    }

    public boolean isLocal(){
        return false;
    }
}
