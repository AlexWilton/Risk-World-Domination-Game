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
	private ArrayList<Country> occupiedCountries;
	private ArrayList<RiskCard> cards;
	private int unassignedArmy;
	///enable if a player is disconnected to skip his turn
	private boolean inactive;

    public Player(int ID){
        this.ID = ID;
    }

    public int getID(){
        return ID;
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

    public ArrayList<Country> getOccupiedCountries(){
        return getOccupiedCountries();
    }

    public void addCard(RiskCard card){
        cards.add(card);
    }

}
