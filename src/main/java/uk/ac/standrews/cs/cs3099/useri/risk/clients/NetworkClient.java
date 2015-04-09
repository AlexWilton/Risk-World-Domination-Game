package uk.ac.standrews.cs.cs3099.useri.risk.clients;

import uk.ac.standrews.cs.cs3099.useri.risk.action.Action;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.Command;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.DefendCommand;

import java.util.ArrayDeque;
import java.util.Queue;

public class NetworkClient extends Client {

    @Override
    public void newSeedComponent() {

    }



    private boolean ready;

    public NetworkClient(State gamestate){
        super(gamestate);
        ready = false;

        this.gameState = gamestate;

    }



    @Override
    public void pushGameState(){

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



    public void setReady (boolean set){
        ready = set;
    }

}
