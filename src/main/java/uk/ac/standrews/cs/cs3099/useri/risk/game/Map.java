package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.util.ArrayList;


public class Map {

    private static final String FILEPATH_DEFAULT_MAP = "data/default.map";

	private ArrayList<Continent> continents;

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

        try {
            String type = (String) mapData.get("data");
            if(type.toLowerCase().equals("map")) throw new Exception();



        }catch(Exception e){
            System.out.println("Error: " + FILEPATH_DEFAULT_MAP + " has invalid map format");
            System.exit(0);
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
}
