package uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.WebClient;

import java.util.ArrayList;

/**
 * represents a player in the game, and keeps track of the occupied countries and risk cards
 * this is just a logical player, doesn't implement any of the mechanisms required for user interaction.
 *
 */
public class Player implements JSONAware{
    private Client client;
    private String name;
    private int ID;
	private CountrySet occupiedCountries;
	private ArrayList<RiskCard> cards;
	private int unassignedArmies;
    private Country countryWhichMustBeDeployedTo  = null;
	///enable if a player is disconnected to skip his turn
	private boolean inactive;

    public Player(int ID, Client client){
        this(ID, client, null);
    }

    public Player(int ID, Client client, String name){
        this.ID = ID;
        this.client = client;
        this.occupiedCountries = new CountrySet();
        this.cards = new ArrayList<RiskCard>();
        this.name = name;
    }

    public int getID(){
        return ID;
    }

    public void addCountry(Country c){
        occupiedCountries.add(c);
    }

    public void removeCountry(Country c){
        occupiedCountries.remove(c);
    }

    public int getUnassignedArmies(){
        return unassignedArmies;
    }

    public void setUnassignedArmies(int value){
        unassignedArmies = value;
    }

    public CountrySet getOccupiedCountries(){
        return occupiedCountries;
    }

    public void addCard(RiskCard card){
        cards.add(card);
    }

    public Client getClient() {
        return client;
    }

    public ArrayList<RiskCard> getCards() {
        return cards;
    }

    /**
     * Choose between countries to place 2 extra armies on when trading in risk cards.
     * @param occ
     * @return
     */
    public Country choose(ArrayList<Country> occ) {
        if(occ.size() > 0) {
            Country chosen = occ.get(0);
            countryWhichMustBeDeployedTo = chosen;
            return chosen; //use first one found - to be improved!
        }
        return null;
    }

    public void removeCards(ArrayList<RiskCard> list) {
        cards.removeAll(list);
    }

    public void occupyCountry (Country c, int troops) {
        c.setOwner(this);
        c.setTroops(troops);
        occupiedCountries.add(c);
    }

    public boolean equals(Object obj) {
        return obj instanceof Player && equals((Player) obj);
    }

    public boolean equals(Player player){
        return (player.getID() == ID);
    }

    /**
     * Gets a risk card from the list of cards this player holds by its id.
     * @param id ID of the risk card that needs to be looked up
     * @return RiskCard with ID id, if the player holds that risk card, null otherwise.
     */
    public RiskCard getRiskCardById(int id){
        for (RiskCard r : cards) {
            if (r.getCardID() == id) {
                return r;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toJSONString() {
        JSONObject player = new JSONObject();
        player.put("name", name);
        player.put("ID", ID);
        player.put("occupiedCountries", occupiedCountries);
        JSONArray cardArray = new JSONArray();
        for(RiskCard c : cards)
            cardArray.add(c);
        player.put("cards", cardArray);
        player.put("unassignedArmies", unassignedArmies);
        player.put("countryWhichMustBeDeployedTo", countryWhichMustBeDeployedTo);
        player.put("inactive", inactive);
        if(client instanceof WebClient){
            WebClient webClient = (WebClient) client;
            player.put("isHost", webClient.isHost());
            player.put("isHostPlaying", webClient.isPlayingHost());
        }
        return player.toJSONString();
    }

    public ArrayList<ArrayList<Integer>> getAllValidCardCombinations(){
        ArrayList<ArrayList<RiskCard>> combos = new ArrayList<>();


        //get all permutations
        for (int i = 0; i<cards.size();i++) {

            for (int j = i + 1; j < cards.size(); j++) {

                for (int k = j + 1; k < cards.size(); k++) {
                    ArrayList<RiskCard> combo = new ArrayList<>();
                    combo.add(cards.get(i));
                    combo.add(cards.get(j));
                    combo.add(cards.get(k));
                    combos.add(combo);
                }
            }
        }

        //remove all that dont have a wild card, are all the same

        ArrayList<ArrayList<Integer>> ret = new ArrayList<>();

        for (ArrayList<RiskCard> combo : combos){


            if (combo.get(0).getType() == combo.get(1).getType() && combo.get(1).getType() == combo.get(2).getType()) {
                //all the same, add
            } else if (combo.get(0).getType() != combo.get(1).getType() && combo.get(1).getType() != combo.get(2).getType() && combo.get(0).getType() != combo.get(2).getType()) {
                //all distinct add
            } else if (combo.get(0).getType() == RiskCardType.TYPE_WILDCARD || combo.get(1).getType() == RiskCardType.TYPE_WILDCARD || combo.get(2).getType() == RiskCardType.TYPE_WILDCARD){
                //wildcard add
            } else {
                continue;
            }

            ArrayList<Integer> numberCombo = new ArrayList<>();

            numberCombo.add(combo.get(0).getCardID());
            numberCombo.add(combo.get(1).getCardID());
            numberCombo.add(combo.get(2).getCardID());

            ret.add(numberCombo);
        }

       return ret;

    }

    public Country getCountryWhichMustBeDeployedTo(){
        Country c = countryWhichMustBeDeployedTo;
        countryWhichMustBeDeployedTo = null;
        return c;
    }

    public CountrySet getOwnedCountriesWithEnemyBoundaries(){
        CountrySet ret = new CountrySet();
        for (Country c : occupiedCountries){
            if (c.getEnemyNeighbours().size()>0)
                ret.add(c);
        }

        return ret;
    }

}
