package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import java.util.ArrayList;

/**
 * represents a country with borders and keeps track of the troops currently deployed and the player that owns it.
 *
 */
public class Country implements JSONAware{

	private String countryName = null;
    private int countryId;
	private CountrySet linkedCountries = new CountrySet();
	private int troops = -1;
	private Player owner =null;

    public Country( int countryId, String countryName){
        this(countryId);
        this.countryName = countryName;
    }

    public Country(int countryId){
        this.countryId = countryId;
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

    public CountrySet getNeighbours() { return linkedCountries;}

    public CountrySet getSamePlayerNeighbours() {

        CountrySet ret = new CountrySet();
        for (Country c : linkedCountries){
            if (this.getOwner() == c.getOwner()){
                ret.add(c);
            }
        }

        return ret;
    }

    public CountrySet getEnemyNeighbours() {

        CountrySet ret = new CountrySet();
        for (Country c : linkedCountries){
            if (this.getOwner() != c.getOwner()){
                ret.add(c);
            }
        }

        return ret;
    }

    public int getCountryId(){
        return countryId;
    }

    @Override
    public String toJSONString() {
        JSONObject country = new JSONObject();
        country.put("name", countryName);
        country.put("country_id", countryId);
        country.put("troop_count", troops);
        if(owner ==null){
            country.put("player_owner_id", "unassigned");
        }else {
            country.put("player_owner_id", owner.getID());
        }
        return country.toJSONString();
    }
}
