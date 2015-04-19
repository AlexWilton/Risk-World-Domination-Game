package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * ContinentSet object containing all the continents
 */
public class ContinentSet  extends HashSet<Continent> implements JSONAware{

    /**
     * Checks whether given Object obj is an instance of the Continent object and whether it is in this ContinentSet or not
     * @param obj : object to be checked
     * @return boolean
     */
    public boolean contains(Object obj){
        if(obj instanceof Continent)
            return contains((Continent) obj);
        return false;
    }

    /**
     * Checks whether given Continent continent is in this particular ContinentSet by comparing the ID.
     * @param continent : Continent object to be checked
     * @return boolean
     */
    public boolean contains(Continent continent){
        for(Continent c : this){
            if(c.getId() == continent.getId()) return true;
        }
        return false;

    }


    /**
     * Returns Continent Object in the continent set with matching given id.
     * @param id
     * @return Continent which matches with the given id
     */
    public Continent get(int id) {
        for(Continent c : this){
            if(c.getId() == id)
                return c;
        }
        return null;
    }

    /**
     * Getter to return the list of ids representing countries
     * @return list of ids that exists in this set
     */
    public ArrayList<Integer> getIDList(){
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for(Continent c : this){
            ids.add(c.getId());
        }
        return ids;
    }

    /**
     * Method to represent the continents in JSON. It
     * recursivelly calls toJSONString on the inner objects until it reaches the
     * concrete object with all the attributes in it.
     * @return String containing JSON representation of the continents
     */
    @Override
    public String toJSONString() {
        JSONArray continents = new JSONArray();
        for(Continent c : this) {
            continents.add(c);
        }
        return continents.toJSONString();
    }
}
