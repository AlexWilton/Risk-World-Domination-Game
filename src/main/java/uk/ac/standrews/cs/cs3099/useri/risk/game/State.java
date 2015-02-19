package uk.ac.standrews.cs.cs3099.useri.risk.game;

import java.util.ArrayList;
import java.util.Stack;

/**
 * holds the game state inclusive map, players and currently active player.
 *
 */
public class State {

	private Map map;
	private ArrayList<Player> players;
    //TODO set this to be a queue or something.
    //TODO: Data structure changed from ArrayList to Stack -> Adjust other part of codes to work with Stack
    private Stack<RiskCard> cardsDeck;
	private Player currentPlayer;
    private Player winner = null;
    private TurnStage stage;
    private boolean wonBattle;
    private int cardSetstradedIn = 0;

    public void setup(Map map, ArrayList<Player> players, Stack<RiskCard> cardsDeck){
        this.map = map;
        this.players = players;
        this.cardsDeck = cardsDeck;
        this.currentPlayer = players.get(0);
        this.stage = TurnStage.STAGE_TRADING;
        wonBattle = false;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean winConditionsMet() {
        //TODO Consider: Extra tests to be on the safe side? making sure nobody is breaking the rule?
        for(Player p : this.players){
            if(p.getOccupiedCountries().size()
                == this.map.getAllCountries().size()){
                this.winner = p;
                return true;
            }
        }
        return false;
    }

    public void nextStage(){
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

    public int getCardSetstradedIn(){
        return cardSetstradedIn;
    }

    public void cardSettradedIn(){
        cardSetstradedIn++;
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

    /**
     * Get a country by id from the list of countries.
     * @param id the ID of the country we are looking for
     * @return Country with ID id, or null if such does not exist.
     */
    public Country getCountryByID(int id) {
        return map.getAllCountries().get(id);
    }

    public ArrayList<Player> getPlayers (){
        return players;
    }
}