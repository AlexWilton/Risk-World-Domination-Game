package uk.ac.standrews.cs.cs3099.useri.risk.game;

import java.util.ArrayList;

/**
 * holds the game state inclusive map, players and currently active player.
 *
 */
public class State {

    public enum TurnStage {
        STAGE_TRADING,
        STAGE_DEPLOYING,
        STAGE_BATTLES,
        STAGE_GET_CARD,
        STAGE_FORTIFY,
        STAGE_FINISH {
            @Override
            public TurnStage next() {
                return TurnStage.STAGE_TRADING;
            }
        };

        public TurnStage next() {
            return values()[ordinal() + 1];
        }
    }

	private Map map;
	private ArrayList<Player> players;
	private Player currentPlayer;
    private Player winner = null;
    private TurnStage stage;

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
}