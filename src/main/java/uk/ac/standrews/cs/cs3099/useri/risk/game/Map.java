package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


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

        HashMap<Integer,Country> countries = parseCountryNames(mapData);

        HashMap<Integer,Continent> continents = parseContinentNames(mapData);

        makeCountryLinks(mapData,countries);

        putCountriesIntoContinent(mapData,countries,continents);

        addContinentValues(mapData,continents);

        this.continents = new ArrayList<Continent>(continents.values());
        System.out.println();




    }

    private HashMap<Integer,Country> parseCountryNames(JSONObject mapData){

        //Parse all Country Data
        HashMap<Integer,Country> countries = new HashMap<Integer, Country>();
        JSONObject countriesJSON = (JSONObject) mapData.get("country_names");

        for (Object o : countriesJSON.keySet()){
            int id = Integer.parseInt(o.toString());
            String name = countriesJSON.get(o).toString();
            countries.put(id,new Country(id,name));
        }

        return countries;
    }

    private HashMap<Integer,Continent> parseContinentNames(JSONObject mapData) {
        //parse all continent data
        HashMap<Integer,Continent> continents = new HashMap<Integer, Continent>();
        JSONObject continentsJSON = (JSONObject) mapData.get("continent_names");

        for (Object o : continentsJSON.keySet()){
            int id = Integer.parseInt(o.toString());
            String name = continentsJSON.get(o).toString();
            continents.put(id, new Continent(id, name));
        }

        return continents;
    }

    private void makeCountryLinks(JSONObject mapData, HashMap<Integer,Country> countries){
        //make country links
        JSONArray connectionsJSON = (JSONArray) mapData.get("connections");

        for (Object connection : connectionsJSON){
            int c0 = Integer.parseInt(((JSONArray) connection).get(0).toString());
            int c1 = Integer.parseInt(((JSONArray) connection).get(1).toString());
            countries.get(c0).addOneWayLinkToCountry(countries.get(c1));
            countries.get(c1).addOneWayLinkToCountry(countries.get(c0));
        }
    }

    private void putCountriesIntoContinent(JSONObject mapData,HashMap<Integer,Country> countries, HashMap<Integer,Continent> continents){
        //put countries into continents
        JSONObject continentMappingsJSON = (JSONObject) mapData.get("continents");

        for (Object continentArrayKey : continentMappingsJSON.keySet()){
            int continentId = Integer.parseInt(continentArrayKey.toString());
            Continent c = continents.get(continentId);
            //go through all countries in the array and add them to the continent
            JSONArray continentCountriesJSON = ((JSONArray) continentMappingsJSON.get(continentArrayKey));
            for (Object countryObject : continentCountriesJSON){
                //find the country
                int countryId = Integer.parseInt(countryObject.toString());
                Country country = countries.get(countryId);
                c.addCountry(country);
            }
        }
    }

    private void addContinentValues(JSONObject mapData, HashMap<Integer,Continent> continents){
        //add continent values
        JSONObject continentValuesJSON = (JSONObject) mapData.get("continent_values");

        for (Object continentValueKey : continentValuesJSON.keySet()){
            int continentId = Integer.parseInt(continentValueKey.toString());
            int value = Integer.parseInt(continentValuesJSON.get(continentValueKey).toString());

            continents.get(continentId).setReinforcementValue(value);
        }
    }




    public ArrayList<Continent> getContinents() {
        return continents;
    }

    public ArrayList<Country> getAllCountries(){
        ArrayList<Country> countries = new ArrayList<Country>();

        for (Continent con : getContinents()){
            for (Country cou : con.getCountries()){
                countries.add(cou);
            }
        }

        return countries;
    }
}
