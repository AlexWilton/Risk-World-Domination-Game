package uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;

import java.util.ArrayList;
import java.util.HashSet;

public class CountrySet extends HashSet<Country> implements JSONAware{

    /**
     * Returns whether this set contains the specified object. The object has to be a Country and must be in the set.
     * @param obj The object to be checked
     * @return true if object is in set.
     */
    public boolean contains(Object obj) {
        return obj instanceof Country && contains((Country) obj);
    }

    /**
     * Returns whether this country is in this set (checking is done by country ID).
     * @param country the Country to be checked
     * @return true if the country is in this set.
     */
    public boolean contains(Country country){
        for(Country c : this){
            if(c.getCountryId() == country.getCountryId()) return true;
        }
        return false;
    }

    /**
     * Returns the country with the given ID, if it's in the set, or null otherwise.
     * @param id The country's ID we're looking for
     * @return Country with specified ID or null if it's not in the set.
     */
    public Country get(int id) {
        for(Country c : this){
            if(c.getCountryId() == id) return c;
        }
        return null;
    }

    /**
     * Returns the complete list of IDs of countries that are in this set.
     * @return possibly empty ArrayList of integer IDs.
     */
    public ArrayList<Integer> getIDList(){
        ArrayList<Integer> ids = new ArrayList<>();
        for(Country c : this){
            ids.add(c.getCountryId());
        }
        return ids;
    }

    @Override
    public String toJSONString() {
        JSONArray countries = new JSONArray();
        for(Country c : this) {
            countries.add(c);
        }
        return countries.toJSONString();
    }
}