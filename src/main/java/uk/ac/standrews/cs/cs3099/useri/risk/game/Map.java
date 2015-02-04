package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;



public class Map {

    private static final String FILEPATH_DEFAULT_MAP = "data/default.map";

	private ContinentSet continents;
    private CountrySet countries;
    private boolean validMap = true;

    public Map(){
        this(FILEPATH_DEFAULT_MAP);
    }

    public Map(String MAP_FILE_PATH){
        JSONObject mapData = null;
        try {
            mapData = (JSONObject) JSONValue.parse(new FileReader(MAP_FILE_PATH));
        } catch (FileNotFoundException e) {
            System.out.println("Map " + MAP_FILE_PATH + " couldn't be found");
            validMap = false;
        }

        //basic check that json contains map data
        try {
            if(!( (String)mapData.get("data")).toLowerCase().equals("map")) validMap = false;
        }catch(NullPointerException e){
            validMap = false;
        }


        countries = parseCountryNames(mapData);

        continents = parseContinentNames(mapData);

        makeCountryLinks(mapData,countries);

        putCountriesIntoContinent(mapData,countries,continents);

        addContinentValues(mapData,continents);

    }

    private CountrySet parseCountryNames(JSONObject mapData){

        //Parse all Country Data
        CountrySet countries = new CountrySet();
        JSONObject countriesJSON = (JSONObject) mapData.get("country_names");

        for (Object o : countriesJSON.keySet()){
            int id = Integer.parseInt(o.toString());
            String name = countriesJSON.get(o).toString();
            countries.add(new Country(id,name));
        }

        return countries;
    }

    private ContinentSet parseContinentNames(JSONObject mapData) {
        //parse all continent data
        ContinentSet continents = new ContinentSet();
        JSONObject continentsJSON = (JSONObject) mapData.get("continent_names");

        for (Object o : continentsJSON.keySet()){
            int id = Integer.parseInt(o.toString());
            String name = continentsJSON.get(o).toString();
            continents.add(new Continent(id, name));
        }

        return continents;
    }

    private void makeCountryLinks(JSONObject mapData, CountrySet countries){
        //make country links
        JSONArray connectionsJSON = (JSONArray) mapData.get("connections");

        for (Object connection : connectionsJSON){
            int c0 = Integer.parseInt(((JSONArray) connection).get(0).toString());
            int c1 = Integer.parseInt(((JSONArray) connection).get(1).toString());
            countries.get(c0).addOneWayLinkToCountry(countries.get(c1));
            countries.get(c1).addOneWayLinkToCountry(countries.get(c0));
        }
    }

    private void putCountriesIntoContinent(JSONObject mapData, CountrySet countries, ContinentSet continents){
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

    private void addContinentValues(JSONObject mapData, ContinentSet continents){
        //add continent values
        JSONObject continentValuesJSON = (JSONObject) mapData.get("continent_values");

        for (Object continentValueKey : continentValuesJSON.keySet()){
            int continentId = Integer.parseInt(continentValueKey.toString());
            int value = Integer.parseInt(continentValuesJSON.get(continentValueKey).toString());

            continents.get(continentId).setReinforcementValue(value);
        }
    }




    public ContinentSet getContinents() {
        return continents;
    }

    public CountrySet getAllCountries(){
        return countries;
    }

    public boolean isValidMap() {
        return validMap;
    }
}
