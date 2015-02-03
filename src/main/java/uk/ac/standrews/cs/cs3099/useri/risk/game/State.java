package uk.ac.standrews.cs.cs3099.useri.risk.game;

import java.util.ArrayList;

/**
 * holds the game state inclusive map, players and currently active player.
 *
 */
public class State {

	private Map map;
	private ArrayList<Player> players;
    //TODO set this to be a queue or something.
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
        wonBattle = false;
    }

    public Player getWinner() {
        return winner;
    }

    public TurnStage getTurnStage(){
        return stage;
    }

    public boolean wonBattle() { return wonBattle; }

    public void winning() {
        wonBattle = true;
    }

    /**
     * Get the top card (index 0, as proposed in the drawing protocol) from the deck and remove it from the deck.
     * @return the top card from the deck.
     */
    public RiskCard getCard() {
        RiskCard c = cardsDeck.get(0);
        cardsDeck.remove(0);
        return c;
    }

}