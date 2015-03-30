package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;

import java.util.ArrayList;
import java.util.HashSet;

public class CountrySet extends HashSet<Country> implements JSONAware{

    public boolean contains(Object obj){
        if(obj instanceof Country)
            return contains((Country) obj);
        return false;
    }

    public boolean contains(Country country){
        for(Country c : this){
            if(c.getCountryId() == country.getCountryId()) return true;
        }
        return false;

    }

    public Country get(int id) {
        for(Country c : this){
            if(c.getCountryId() == id) return c;
        }
        return null;
    }

    public ArrayList<Integer> getIDList(){
        ArrayList<Integer> ids = new ArrayList<Integer>();
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