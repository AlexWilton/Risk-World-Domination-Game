package uk.ac.standrews.cs.cs3099.useri.risk.game;

import java.util.ArrayList;

/**
 * holds the list of countries contained in a continent and the reward for conquering it
 *
 */
public class Continent {
	private ArrayList<Country> countries = new ArrayList<Country>();
	private int reinforcementValue = 0;
    private String name;
    private int id;

    public Continent(int id,String continentName){
        name = continentName;
        this.id = id;
    }

    public void addCountry(Country country){
        countries.add(country);
    }

    public void setReinforcementValue(int reinforcementValue){
        this.reinforcementValue = reinforcementValue;
    }

    public ArrayList<Country> getCountries() {
        return countries;
    }
}
