package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

/**
 * represents a country with borders and keeps track of the troops currently deployed and the player that owns it.
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
        if (troops < 0)
            System.err.println("stuff");
        this.troops = troops;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public CountrySet getNeighbours() { return linkedCountries;}

    /**
     * Get the set of countries that are directly connected with this country and are owned by the same player.
     * @return CountrySet, may be empty.
     */
    public CountrySet getSamePlayerNeighbours() {
        CountrySet ret = new CountrySet();
        for (Country c : linkedCountries){
            if (this.getOwner() == c.getOwner()){
                ret.add(c);
            }
        }
        return ret;
    }

    public CountrySet getNeighboursOwnedBy(int id) {
        CountrySet ret = new CountrySet();
        for (Country c : getNeighbours()){
            if (c.getOwner().getID() == id){
                ret.add(c);
            }
        }

        return ret;
    }

    public Country getClosestCountryOwnedBy(int id) {
        CountrySet currConnected = this.getNeighbours();
        while (true){
            CountrySet newCurrConnected = new CountrySet();
            for (Country c : currConnected) {
                if (c.getOwner().getID() == id) {
                    return c;
                }
                newCurrConnected.addAll(c.getNeighbours());
            }
            currConnected = newCurrConnected;


        }
    }

    /**
     * Get the set of countries that are directly connected with this country and are owned by a different player.
     * @return CountrySet, may be empty.
     */
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
