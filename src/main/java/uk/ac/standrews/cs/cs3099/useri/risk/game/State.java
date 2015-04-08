package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import uk.ac.standrews.cs.cs3099.risk.game.RandomNumbers;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.RNGSeed;

import java.util.ArrayList;
import java.util.Stack;

/**
 * holds the game state inclusive map, players and currently active player.
 *
 */
public class State implements JSONAware{

	private Map map;
	private ArrayList<Player> players;
    //TODO set this to be a queue or something.
    //TODO: Data structure changed from ArrayList to Stack -> Adjust other part of codes to work with Stack
    private Stack<RiskCard> cardsDeck;
	private Player currentPlayer;
    private Player winner = null;
    private TurnStage stage = TurnStage.STAGE_TRADING;;
    private boolean wonBattle = false;
    private int cardSetstradedIn = 0;

    public State(){}

    public State(Map map, ArrayList<Player> players){
        setup(map, players);
    }

    public void setup(Map map, ArrayList<Player> players){
        this.map = map;
        this.players = players;
        this.cardsDeck = map.getCardsFromMapData();
        this.currentPlayer = players.get(0);
    }

    public void setCurrentPlayer(int playerId){
        currentPlayer = getPlayer(playerId);
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean winConditionsMet() {
        //TODO Consider: Extra tests to be on the safe side? making sure nobody is breaking the rule?
        for(Player p : players){
            if(p.getOccupiedCountries().size()
                == map.getAllCountries().size()){
                winner = p;
                return true;
            }
        }
        return false;
    }

    public void nextStage(){
        stage = stage.next();
    }

    public void endTurn(){
        nextPlayer();
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

    public Player getPlayer(int playerId) {
        for (Player p:players){
            if (p.getID() == playerId)
                return p;
        }
        return null;
    }

    @Override
    public String toJSONString() {
        JSONObject state = new JSONObject();
        state.put("map", map);
        JSONArray playerJson = new JSONArray();
        for(Player p : players)
            playerJson.add(p);
        state.put("players", playerJson);
        state.put("currentPlayer", currentPlayer);
        JSONArray deckArray = new JSONArray();
        for(RiskCard card : cardsDeck)
            deckArray.add(card);
        state.put("cardDeck", deckArray);
        state.put("winner", winner);
        state.put("turn_stage", stage.toString());
        state.put("wonBattle", wonBattle);
        return state.toJSONString();
    }

    public void shuffleRiskCards(RNGSeed seed){
        RandomNumbers rng = new RandomNumbers(seed.getHexSeed());

        //cards: Array containing all cards.

        int len = cardsDeck.size();

        for (int i = 0; i<len;i++){
            int swapPos = rng.getRandomByte();
            RiskCard c1 = cardsDeck.get(i);
            cardsDeck.set(i,cardsDeck.get(swapPos));
            cardsDeck.set(swapPos,c1);
        }
    }

    public boolean hasUnassignedCountries(){
        return map.hasUnassignedCountries();
    }

    public void nextPlayer(){
        currentPlayer = getPlayer((currentPlayer.getID()+1)%getPlayerAmount());
    }

    public int getPlayerAmount(){
        return getPlayers().size();
    }


}