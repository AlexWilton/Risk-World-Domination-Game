package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;

import java.util.ArrayList;
import java.util.HashSet;

public class ContinentSet  extends HashSet<Continent> implements JSONAware{

    public boolean contains(Object obj){
        if(obj instanceof Continent)
            return contains((Continent) obj);
        return false;
    }

    public boolean contains(Continent continent){
        for(Continent c : this){
            if(c.getId() == continent.getId()) return true;
        }
        return false;

    }

    public Continent get(int id) {
        for(Continent c : this){
            if(c.getId() == id)
                return c;
        }
        return null;
    }

    public ArrayList<Integer> getIDList(){
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for(Continent c : this){
            ids.add(c.getId());
        }
        return ids;
    }

    @Override
    public String toJSONString() {
        JSONArray continents = new JSONArray();
        for(Continent c : this) {
            continents.add(c);
        }
        return continents.toJSONString();
    }
}
