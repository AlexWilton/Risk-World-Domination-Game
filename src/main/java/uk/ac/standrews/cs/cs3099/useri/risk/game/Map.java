package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;


public class Map {

    private static final String FILEPATH_DEFAULT_MAP = "data/default.map";

	private ArrayList<Continent> continents;
    private boolean validMap = true;

    public Map(ArrayList<Continent> continents){
        this.continents = continents;
    }


    /**
     * Load default Map
     */
    public Map(){
        JSONParser parser=new JSONParser();
        JSONObject mapData = null;

        try {
            mapData = (JSONObject) JSONValue.parse(new FileReader(FILEPATH_DEFAULT_MAP));
        } catch (Exception e) {
            e.printStackTrace();
        }


            String type = (String) mapData.get("data");
            if(!type.toLowerCase().equals("map")) validMap = false;


        //Parse all Country Data store by key
        ArrayList<Country> countries = new ArrayList<Country>();
        JSONObject continentsJSON = (JSONObject) mapData.get("continents");
        int largestCountryID = -1;
        for(Object continentObj : continentsJSON.values()){
            JSONArray continentJSON = (JSONArray) continentObj;
            for(Object countryID : continentJSON){
                countries.add(new Country((Integer) countryID));
            }
        }





//
//        for (Object o : a)
//        {
//            JSONObject attr = (JSONObject) o;
//
//            String dataType = (String) person.get("data");
//            System.out.println(name);
//
//            String city = (String) person.get("city");
//            System.out.println(city);
//
//            String job = (String) person.get("job");
//            System.out.println(job);
//
//            JSONArray cars = (JSONArray) person.get("cars");
//
//        }

    }

    public ArrayList<Continent> getContinents() {
        return continents;
    }
}
