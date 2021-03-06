package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.*;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.RandomNumberGenerator;

import java.util.ArrayList;
import java.util.Stack;

/**
 * holds the game state inclusive map, available cards, players and currently active player.
 *
 */
public class State implements JSONAware {

    private Map map;
    private ArrayList<Player> players;
    private Stack<RiskCard> cardsDeck;
    private Player currentPlayer;
    private Player firstPlayer;
    private Player winner = null;
    private TurnStage stage = TurnStage.STAGE_TRADING;
    private boolean wonBattle = false;
    private int cardSetstradedIn = 0;
    private boolean preGamePlay = true;

    private int turns = 0;

    private CountrySet setOfCountriesWhereAtLeastOneNeedsToBeDeployedTo = null;

    private boolean attackCaptureNeeded = false;
    private Country attackCaptureOrigin = null;
    private Country attackCaptureDestination = null;
    private int attackCaptureMinimumArmiesToMove = -1;

    private ArrayList<Integer> lostPlayers = new ArrayList<>();



    public State(){}

    public State(Map map, ArrayList<Player> players){
        setup(map, players);

    }

    public Continent getCountryContinent(int country){
        for (Continent c : map.getContinents()){
            if (c.getCountries().contains(getCountryByID(country))){
                return c;
            }
        }

        return null;
    }



    public void setup(Map map, ArrayList<Player> players){
        this.map = map;
        this.players = players;
        this.cardsDeck = map.getCardsFromMapData();
        this.currentPlayer = players.get(0);

        //set number of initial troops for each player (based on the total number of players in the game)
        int armiesForEachPlayer = 20 + (6 - players.size()) * 5;
        for(Player p : players){
            p.setUnassignedArmies(armiesForEachPlayer);
        }
    }

    public void setCurrentPlayer(int playerId){
        currentPlayer = getPlayer(playerId);
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Continent getContinentById (int id){
        return getContinents().get(id);
    }

    public boolean winConditionsMet() {
        for(Player p : players){
            if(p.getOccupiedCountries().size()
                == map.getAllCountries().size()){
                winner = p;
                return true;
            }
        }
        return false;
    }

    public void nextStage() {
        stage = stage.next();
    }

    public void endTurn() {
        nextPlayer();
        if(!preGamePlay)
            preTurnCalculateUnassignedArmies(currentPlayer);
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

    public void territoryCaptured() {
        wonBattle = true;
    }

    public int getCardSetstradedIn(){
        return cardSetstradedIn;
    }

    public void cardSettradedIn(){
        cardSetstradedIn++;
    }

    public void removePlayer(int playerId) {
        for (int i = 0; i<players.size(); i++) {
            if (players.get(i).getID() == playerId) {

                players.remove(i);

                lostPlayers.add(playerId);
                break;
            }
        }
    }



    /**
     * Get the top card (index 0, as proposed in the drawing protocol) from the deck and remove it from the deck.
     * @return the top card from the deck.
     */
    public RiskCard getCard() {
        RiskCard c = peekCard();
        if(c != null){
            cardsDeck.remove(0);
        }

        return c;
    }

    public RiskCard peekCard() {
        if (cardsDeck.size() == 0)
            return null;
        RiskCard c = cardsDeck.get(0);

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

    ContinentSet getContinents(){
        return map.getContinents();
    }

    private ArrayList<Player> getPlayers (){
        return players;
    }

    public boolean anyoneHasUnassignedArmies() {
        for (Player p : players) {
            if (p.getUnassignedArmies() != 0) {
                return true;
            }
        }
        return false;
    }

    public int getRemovedPlayer() {
        for (Player p : players) {
            if (p.getOccupiedCountries().size() == 0 && !isPreGamePlay())
                return p.getID();
        }
        return -1;
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
        boolean areAllCountriesClaimed = true;
        for(Country c : map.getAllCountries()){
            if(c.getOwner() == null) areAllCountriesClaimed = false;

        }
        state.put("all_countries_claimed", areAllCountriesClaimed);
        state.put("pre_game_play", preGamePlay);
        state.put("attack_capture_needed", attackCaptureNeeded);
        state.put("attack_capture_origin", attackCaptureOrigin);
        state.put("attack_capture_destination", attackCaptureDestination);
        state.put("attack_capture_min_armies_to_move_in", attackCaptureMinimumArmiesToMove);
        JSONArray countriesWhereAtLeastOneNeedsToBeDeployedTo = new JSONArray();
        if(setOfCountriesWhereAtLeastOneNeedsToBeDeployedTo != null){
            for(Country c : setOfCountriesWhereAtLeastOneNeedsToBeDeployedTo)
                countriesWhereAtLeastOneNeedsToBeDeployedTo.add(c);
        }
        state.put("setOfCountriesWhereAtLeastOneNeedsToBeDeployedTo", countriesWhereAtLeastOneNeedsToBeDeployedTo);
        return state.toJSONString();
    }

    public void endPreGame(){
        preGamePlay = false;
        currentPlayer = firstPlayer;
        preTurnCalculateUnassignedArmies(currentPlayer);
        stage = TurnStage.STAGE_FINISH.next();
        wonBattle = false;
    }

    public boolean isPreGamePlay(){
        return preGamePlay;
    }
    
    public void shuffleRiskCards(RandomNumberGenerator seed){
        RandomNumberGenerator rng = new RandomNumberGenerator();

        //cards: Array containing all cards.

        int len = cardsDeck.size();

        for (int i = 0; i<len;i++){
            int swapPos = (int)(seed.nextInt()%len);
            RiskCard c1 = cardsDeck.get(i);
            cardsDeck.set(i,cardsDeck.get(swapPos));
            cardsDeck.set(swapPos,c1);
        }
    }

    public boolean hasUnassignedCountries(){
        return map.hasUnassignedCountries();
    }

    public CountrySet unoccupiedCountries(){
        CountrySet ret = new CountrySet();
        for (Country c : map.getAllCountries()){
            if (c.getOwner() == null){
                ret.add(c);
            }
        }

        return ret;
    }

    public void nextPlayer(){
        int i = currentPlayer.getID();
        int maxPlayerId = -1;
        for (Player p : players){
            if (p.getID() > maxPlayerId)
                maxPlayerId = p.getID();
        }
        do {
            currentPlayer = getPlayer((++i) % (maxPlayerId+1));
        } while (currentPlayer == null);

        turns++;
    }

    int getTurns(){
        return turns;
    }
    int getPlayerAmount(){
        return getPlayers().size();
    }


    public void setFirstPlayer(Player firstPlayer) {
        this.firstPlayer = firstPlayer;
    }

    public Player getFirstPlayer() {
        return firstPlayer;
    }

    void preTurnCalculateUnassignedArmies(Player player) {
        if (player == null)
            return;
        int territoryCount = player.getOccupiedCountries().size();
        int amount = territoryCount / 3;
        if(amount < 3) amount = 3;

        for(Continent continent : getContinents()){
            boolean wholeContinentOwned = true;
            for(Country country : continent.getCountries()){
                if(country.getOwner() != player){
                    wholeContinentOwned = false;
                }
            }
            if(wholeContinentOwned)
                amount += continent.getReinforcementValue();
        }

        player.setUnassignedArmies(amount);
    }

    public CountrySet getAllUnassignedCountries(){
        return map.getUnassignedCountries();
    }

    public void setTurnStage(TurnStage s){
        this.stage = s;
    }

    public boolean isAttackCaptureNeeded() {
        return attackCaptureNeeded;
    }

    public Country getAttackCaptureOrigin() {
        return attackCaptureOrigin;
    }

    public Country getAttackCaptureDestination() {
        return attackCaptureDestination;
    }

    public int getAttackCaptureMinimumArmiesToMove() {
        return attackCaptureMinimumArmiesToMove;
    }

    public void recordAttackCaptureIsNeeded(Country attackCaptureOrigin, Country attackCaptureDestination, int attackCaptureMinimumArmiesToMove) {
        attackCaptureNeeded = true;
        this.attackCaptureOrigin = attackCaptureOrigin;
        this.attackCaptureDestination = attackCaptureDestination;
        this.attackCaptureMinimumArmiesToMove = attackCaptureMinimumArmiesToMove;
    }

    public void markAttackCaptureNotNeeded() {
        attackCaptureNeeded = false;
        attackCaptureOrigin = null;
        attackCaptureDestination = null;
        attackCaptureMinimumArmiesToMove = -1;
    }

    public CountrySet getAllCountries() {
        return map.getAllCountries();
    }

    public CountrySet getAllCountriesInMap(){
        return map.getAllCountries();
    }

    public CountrySet getSetOfCountriesWhereAtLeastOneNeedsToBeDeployedTo() {
        return setOfCountriesWhereAtLeastOneNeedsToBeDeployedTo;
    }

    public void setSetOfCountriesWhereAtLeastOneNeedsToBeDeployedTo(CountrySet setOfCountriesWhereAtLeastOneNeedsToBeDeployedTo) {
        this.setOfCountriesWhereAtLeastOneNeedsToBeDeployedTo = setOfCountriesWhereAtLeastOneNeedsToBeDeployedTo;
    }

    /**
     * calculates player score
     * @param id
     * @return
     */
    public int getPlayerPoints(int id ){
        if (players.size() == 1) {
            if (lostPlayers.contains(id))
                return lostPlayers.indexOf(id);
            else
                return lostPlayers.size() *2;
        }
        else {
            if (lostPlayers.contains(id)){
                return lostPlayers.indexOf(id);
            }
            int worsePlayers = lostPlayers.size();
            int troops = 0;
            if (getPlayer(id) != null)
                troops = getPlayer(id).sumAllTroops();

            for (Player p : players){
                if (p.sumAllTroops() < troops){
                    worsePlayers ++;
                }
            }
            return worsePlayers+1;
        }
    }

    /**
     * gets rank of player after game
     * @param id
     * @return
     */
    public int getPlayerRank(int id) {
        int players = lostPlayers.size()+ getPlayers().size();
        int worsePlayers = 0;
        boolean pContains = false;

        for (Player p : getPlayers()){
            if (p.getID() == id){
                pContains = true;
            }
        }
        if (pContains){
            worsePlayers = lostPlayers.size();
            int troops = 0;
            if (getPlayer(id) != null)
                troops = getPlayer(id).sumAllTroops();

            for (Player p : getPlayers()){
                if (p.sumAllTroops() < troops){
                    worsePlayers ++;
                }
            }

        } else {
            worsePlayers = lostPlayers.indexOf(id) ;
        }

        return players-worsePlayers;
    }
}