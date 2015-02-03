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
    private int cardSetstradedIn = 0;

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
        ArrayList<Continent> continents = map.getContinents();
        for (Continent cont:continents){
            ArrayList<Country> countries = cont.getCountries();
            for (Country country:countries){
                if (country.getCountryId() == id){
                    return country;
                }
            }
        }
        return null;

    }
}