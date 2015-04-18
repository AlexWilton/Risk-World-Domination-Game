package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Stack;

/**
 * Map that holds a list of all countries and continents.
 */
public class Map implements JSONAware{

    private static final String DEFAULT_MAP_PATH = "src/res/defaultMap.json";

	private ContinentSet continents;
    private CountrySet countries;
    private boolean validMap = true;
    private JSONObject mapData;

    //uses default map'
    public Map(){
        this(DEFAULT_MAP_PATH);
    }

    //uses given filename
    public Map(String MAP_FILE_PATH){
        mapData = null;
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

    /**
     * Parse the countries from the Map JSON object.
     * @param mapData JSONObject to parse
     * @return Countries in the map.
     */
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

    /**
     * Parse the continents from the Map JSON Object
     * @param mapData The JSONObject to parse
     * @return Continents in the map.
     */
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

    /**
     * Parses the map JSON and sets up the connections between the countries that are adjacent to each other.
     * @param mapData JSON to be parsed
     * @param countries Set of countries to apply the links on.
     */
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

    /**
     * Parses the JSON object and puts each country in the continent it belongs to
     * @param mapData JSON to be parsed
     * @param countries Set of countries to be put in continents.
     * @param continents Set of continents to put countries into.
     */
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

    /**
     * Adds a value to each continent, ie. the amount of armies a player will obtain for owning all countries in the
     * continent.
     * @param mapData The JSON to be parsed
     * @param continents The set of continents to be given values.
     */
    private void addContinentValues(JSONObject mapData, ContinentSet continents){
        //add continent values
        JSONObject continentValuesJSON = (JSONObject) mapData.get("continent_values");

        for (Object continentValueKey : continentValuesJSON.keySet()){
            int continentId = Integer.parseInt(continentValueKey.toString());
            int value = Integer.parseInt(continentValuesJSON.get(continentValueKey).toString());

            continents.get(continentId).setReinforcementValue(value);
        }
    }

    /**
     * Parse the cards of the map. In the default map, each country represents a risk card as well, whose ID should be
     * the same. Each card has a type, artillery, cavalry or infantry.
     * @return A stack of Risk cards.
     */
    private Stack<RiskCard> parseCountryCards(){
        Stack<RiskCard> cards = new Stack<>();
        if(mapData==null){
            System.err.println("MapData not initialised;");
            return null;
        }else{
            JSONObject cardObject = (JSONObject) mapData.get("country_card");
            for(Object key : cardObject.keySet()){
                RiskCard tempCard = null;
                int country_id = Integer.parseInt(key.toString());
                int card_type_id = Integer.parseInt(cardObject.get(key).toString());
                switch(card_type_id){
                    case 0: tempCard = new RiskCard(RiskCardType.TYPE_INFANTRY, country_id);
                            break;
                    case 1: tempCard = new RiskCard(RiskCardType.TYPE_CAVALRY, country_id);
                            break;
                    case 2: tempCard = new RiskCard(RiskCardType.TYPE_ARTILLERY, country_id);
                            break;
                }
                if(tempCard!=null) {
                    cards.push(tempCard);
                }else{
                    System.err.println("Invalid Card Type identifier");
                }
            }
        }
        return cards;
    }

    public ContinentSet getContinents() {
        return continents;
    }

    public CountrySet getAllCountries(){
        return countries;
    }

    public Stack<RiskCard> getCardsFromMapData(){
        return parseCountryCards();
    }

    public boolean isValidMap() {
        return validMap;
    }

    public boolean hasUnassignedCountries(){
        return getUnassignedCountries().size() > 0;
    }

    /**
     * Checks all countries and whether they have an owner.
     * @return The list of countries without an owner.
     */
    public CountrySet getUnassignedCountries(){
        CountrySet ret = new CountrySet();
        for (Country c : countries){
            if (c.getOwner() == null){
                ret.add(c);
            }
        }
        return ret;
    }

    @Override
    public String toJSONString() {
        JSONObject map = new JSONObject();
        map.put("countries", countries);
        map.put("continents", continents);
        return map.toJSONString();
    }
}
