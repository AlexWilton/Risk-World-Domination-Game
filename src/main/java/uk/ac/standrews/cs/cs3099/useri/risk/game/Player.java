package uk.ac.standrews.cs.cs3099.useri.risk.game;

import uk.ac.standrews.cs.cs3099.useri.risk.action.Action;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;

import java.util.ArrayList;

/**
 * represents a player in the game, and keeps track of the occupied countries and risk cards
 *
 */
public class Player{
    private Client client;
    private int ID;
	private CountrySet occupiedCountries;
	private ArrayList<RiskCard> cards;
	private int unassignedArmy;
	///enable if a player is disconnected to skip his turn
	private boolean inactive;

    public Player(int ID, Client client){
        this.ID = ID;
        this.client = client;
        this.occupiedCountries = new CountrySet();
        this.cards = new ArrayList<RiskCard>();
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

    public Action getPlayerAction(){
        return client.getAction();
    }

    public int getUnassignedArmy (){
        return unassignedArmy;
    }

    public void setUnassignedArmy(int value){
        unassignedArmy = value;
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
}
