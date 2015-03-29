package uk.ac.standrews.cs.cs3099.useri.risk.clients;

import uk.ac.standrews.cs.cs3099.useri.risk.action.Action;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;

import java.util.Queue;

public class NetworkClient extends Client {


    private Queue<Action> actionQueue;

    private boolean ready;

    public NetworkClient(){
        ready = false;
    }

    @Override
    public Action getAction () {
        //wait until an action is in the queue
        while (actionQueue.isEmpty());
        //the return the first
        return actionQueue.remove();
    }

    @Override
    public void pushGameState(){

    }

    @Override
    public int[] getSeedComponent(){

        return null;
    }

    @Override
    public int getDefenders(Country attacker, Country objective, int attackingArmies) {
        return 0;
    }

    public void pushAction(Action action){
        actionQueue.add(action);
    }

    @Override
    public boolean isReady(){
        return ready;
    }

    public void setReady (boolean set){
        ready = set;
    }

}
