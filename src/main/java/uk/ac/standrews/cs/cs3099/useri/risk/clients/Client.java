package uk.ac.standrews.cs.cs3099.useri.risk.clients;

import uk.ac.standrews.cs.cs3099.useri.risk.action.Action;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;

/**
 * represents one client.
 * can take multiple forms, eg local, network or AI
 *
 */
public abstract class Client {


    protected State gameState;

    private int playerId;

    /**
     * @return the next action this player takes based on current game state
     */
    public abstract Action getAction();

    /**
     * notify player that game state has changed
     */
    public abstract void pushGameState();

    public abstract int getDefenders(Country attackingCountry, Country defendingCountry, int attackingArmies);

    public abstract int[] getSeedComponent();

    public void setPlayerId(int playerId){
        this.playerId = playerId;
    }

    public int getPlayerId(){
        return playerId;
    }

    public Player getPlayer() {
        return (gameState == null)? null : gameState.getPlayer(playerId);
    }
}
