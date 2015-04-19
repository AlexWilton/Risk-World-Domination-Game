package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

/**
 * holds the list of countries contained in a continent and the reward for conquering the whole continent.
 */
public class Continent implements JSONAware {
	private CountrySet countries = new CountrySet();
	private int reinforcementValue = 0;
    private String name;
    private int id;

    /**
     * Constructor of the Continent Object
     * @param id integer defining the continent
     * @param continentName String defining name of the continent represented
     */
    public Continent(int id,String continentName) {
        name = continentName;
        this.id = id;
    }

    /**
     * Method to add country passed into a continent into countryset object contained in continent
     * @param country object representing country
     */
    public void addCountry(Country country) {
        countries.add(country);
    }

    /**
     *
     * @param reinforcementValue
     */
    public void setReinforcementValue(int reinforcementValue) {
        this.reinforcementValue = reinforcementValue;
    }

    public CountrySet getCountries() {
        return countries;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getReinforcementValue() {
        return reinforcementValue;
    }

    @Override
    public String toJSONString() {
        JSONObject continent = new JSONObject();
        continent.put("countries", countries);
        continent.put("reinforcementValue", reinforcementValue);
        continent.put("name", name);
        continent.put("continent_id", id);
        return continent.toJSONString();
    }
}
