package uk.ac.standrews.cs.cs3099.useri.risk.game;

import java.util.HashSet;

public class CountrySet extends HashSet<Country>{

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
}