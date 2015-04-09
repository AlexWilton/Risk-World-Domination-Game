package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import uk.ac.standrews.cs.cs3099.useri.risk.action.Action;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;

import java.util.ArrayList;

/**
 * represents a player in the game, and keeps track of the occupied countries and risk cards
 *
 */
public class Player implements JSONAware{
    private Client client;
    private String name;
    private int ID;
	private CountrySet occupiedCountries;
	private ArrayList<RiskCard> cards;
	private int unassignedArmies;
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

    public ArrayList<RiskCard> getCards() { return cards; }

    /**
     * Choose between countries to place 2 extra armies on when trading in risk cards.
     * @param occ
     * @return
     */
    public Country choose(ArrayList<Country> occ) {
        if(occ.size() > 0) return occ.get(0); //use first one found - to be improved!
        //TODO unimplemented method, required for TradeAction!
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

    public boolean equals(Object obj){
        return obj instanceof Player && equals( (Player) obj);
    }

    public boolean equals(Player player){
        if(player.getID() == ID)
            return true;
        return false;
    }

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
        player.put("inactive", inactive);
        return player.toJSONString();
    }
}
