package uk.ac.standrews.cs.cs3099.useri.risk.game;

import java.util.ArrayList;

/**
 * represents a country with borders and keeps track of the troops currently deployed and the player that owns it.
 *
 */
public class Country {

	private String countryName;
    private int countryId;
	private ArrayList<Country> linkedCountries;
	private int troops;
	private Player owner;

    public Country( int countryId, String countryName){
        this(countryId);
        this.countryName = countryName;
    }

    public Country(int countryId){
        this.countryId = countryId;
    }

    public void addTwoWayLinkToCountry(Country otherCountry){
        linkedCountries.add(otherCountry);
        otherCountry.addTwoWayLinkToCountry(this);
    }

    public void addOneWayLinkToCountry(Country otherCountry){
        linkedCountries.add(otherCountry);
    }

    public int getTroops() {
        return troops;
    }

    public String getCountryName() {
        return countryName;
    }

    public Player getOwner() {
        return owner;
    }

    public void setTroops(int troops) {
        this.troops = troops;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public ArrayList<Country> getNeighbours() { return linkedCountries;}
}
