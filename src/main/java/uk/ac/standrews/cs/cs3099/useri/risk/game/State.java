package uk.ac.standrews.cs.cs3099.useri.risk.game;

import java.util.ArrayList;

/**
 * holds the game state inclusive map, players and currently active player.
 *
 */
public class State {

    public enum TurnStage {STAGE_TRADING, STAGE_DEPLOYING, STAGE_BATTLES, STAGE_FORTIFY}

	private Map map;
	private ArrayList<Player> players;
	private Player currentPlayer;
    private Player winner = null;
	private int turnCount;
    private TurnStage stage;

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean winConditionsMet() {
        //TODO if there is a winner, set winner and return true

        return false;
    }

    public Player getWinner() {
        return winner;
    }

    public TurnStage getTurnStage(){
        return stage;
    }
}