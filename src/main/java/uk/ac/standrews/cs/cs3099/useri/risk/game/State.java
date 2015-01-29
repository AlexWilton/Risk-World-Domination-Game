package uk.ac.standrews.cs.cs3099.useri.risk.game;

import java.util.ArrayList;

/**
 * holds the game state inclusive map, players and currently active player.
 *
 */
public class State {

	private Map map;
	private ArrayList<Player> players;
	private Player currentPlayer;
    private Player winner = null;
	private int turnCount;

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
}