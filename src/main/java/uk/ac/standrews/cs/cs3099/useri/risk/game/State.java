package uk.ac.standrews.cs.cs3099.useri.risk.game;

import java.util.ArrayList;

/**
 * holds the game state inclusive map, players and currently active player.
 *
 */
public class State {

	private Map map;
	private ArrayList<Player> players;
    private ArrayList<RiskCard> cardsDeck;
	private Player currentPlayer;
    private Player winner = null;
    private TurnStage stage;
    private boolean wonBattle;

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean winConditionsMet() {
        //TODO if there is a winner, set winner and return true

        return false;
    }

    public void nextAction(){
        stage = stage.next();
    }

    public void endTurn(){
        currentPlayer = players.get((players.indexOf(currentPlayer) + 1) % players.size());
        stage = TurnStage.STAGE_FINISH.next();
    }

    public Player getWinner() {
        return winner;
    }

    public TurnStage getTurnStage(){
        return stage;
    }

    public boolean wonBattle() { return wonBattle; }

    /**
     * Get the top card from the deck and remove it from the deck.
     * @return the top card from the deck.
     */
    public RiskCard getCard() {
        RiskCard c = cardsDeck.get(cardsDeck.size() - 1);
        cardsDeck.remove(c);
        return c;
    }
}