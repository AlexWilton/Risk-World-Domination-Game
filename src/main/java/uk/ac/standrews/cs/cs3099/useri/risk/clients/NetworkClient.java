package uk.ac.standrews.cs.cs3099.useri.risk.clients;

import uk.ac.standrews.cs.cs3099.useri.risk.action.Action;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;

import java.util.ArrayDeque;
import java.util.Queue;

public class NetworkClient extends Client {

    @Override
    public void newSeedComponent() {

    }

    private Queue<Action> actionQueue;

    private boolean ready;

    public NetworkClient(State gamestate){
        ready = false;
        actionQueue = new ArrayDeque<>();

        this.gameState = gamestate;

    }

    @Override
    public Action getAction () {
        //wait until an action is in the queue
        while (actionQueue.isEmpty()){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //the return the first
        return actionQueue.remove();
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
