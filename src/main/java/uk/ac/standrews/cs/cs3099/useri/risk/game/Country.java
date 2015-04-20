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

    /**
     * Constructor of the country object
     * @param countryId integer value representing the id of the country
     * @param countryName String value to represent the name of the country
     */
    public Country( int countryId, String countryName){
        this(countryId);
        this.countryName = countryName;
    }

    /**
     * Constructor of the country object without country name - used in testing
     * @param countryId integer varialbe representing the id of the country
     */
    public Country(int countryId){
        this.countryId = countryId;
    }

    /**
     * Adds link to the country passed in as the variable
     * @param otherCountry Country object to be linked to
     */
    public void addOneWayLinkToCountry(Country otherCountry){
        linkedCountries.add(otherCountry);
    }

    /**
     * Method to retrieve number of troops in the country
     * @return number of troops in the country as integer
     */
    public int getTroops() {
        return troops;
    }
    /**
     * Setter for number of troops in the country
     * @param troops integer
     */
    public void setTroops(int troops) {
        if (troops < 0)
            System.err.println("stuff");
        this.troops = troops;
    }

    /**
     * Method to get country name
     * @return String variable representing the name of the country
     */
    public String getCountryName() {
        return countryName;
    }

    /**
     * Getter for country ID
     * @return Integer country ID variable
     */
    public int getCountryId(){
        return countryId;
    }

    /**
     * Getter for the player who owns the country
     * @return Player object representing the owner
     */
    public Player getOwner() {
        return owner;
    }

    /**
     * Setter of the owner of the country
     * @param owner Player object representing the owner of the country
     */
    public void setOwner(Player owner) {
        this.owner = owner;
    }

    /**
     * Returns all of the neighbours connected to this country
     * @return CountrySet object containing all the countries connected to this country
     */
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

    public CountrySet getNeighboursNotOwnedBy(int id) {
        CountrySet ret = new CountrySet();
        for (Country c : getNeighbours()){
            if (c.getOwner().getID() != id){
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

    /**
     * Method fo generate JSON string representation of the country
     * @return String JSON representation
     */
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
