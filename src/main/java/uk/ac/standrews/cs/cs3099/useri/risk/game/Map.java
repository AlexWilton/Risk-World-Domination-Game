package uk.ac.standrews.cs.cs3099.useri.risk.game;

import java.util.ArrayList;

public class Map {
	
	private ArrayList<Continent> continents;

    public Map(ArrayList<Continent> continents){
        this.continents = continents;
    }


    /**
     * Load default Map
     */
    public Map(){
        Continent northAmerica = new Continent();
        northAmerica.setReinforcementValue(5);
        Country alaska = new Country(0, "Alaska");
        Country northwestTerritory = new Country(1, "Northwest Territory");
        Country greenland = new Country(2, "Greenland");
        Country alberta = new Country(3, "Alberta");
        Country ontario = new Country(4, "Ontario");
        Country quebec = new Country(5, "Quebec");
        Country westUS = new Country(6, "Western United States");
        Country eastUS = new Country(7, "Eastern United States");
        //decided to write directly into json instead... (Alex)
    }
}
