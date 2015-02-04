package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;



public class MapTest {

    private Map defaultMap;
    private CountrySet defaultCountries;
    private ContinentSet defaultContinents;

    @Before
    public void setup(){
        defaultMap = new Map();
        defaultCountries = defaultMap.getAllCountries();
        defaultContinents = defaultMap.getContinents();
    }

    @Test
    public void canLoadDefaultMap() {
        assertTrue(defaultMap.isValidMap());

        CountrySet countries = defaultMap.getAllCountries();
        ContinentSet continents = defaultMap.getContinents();
        assertTrue(countries != null);
        assertTrue(continents != null);
    }

    @Test
    public void correctCountriesAndContinentsPresentInDefault(){
        //Check that the first and last countries and continents are present
        Country alaska = new Country(0, "Alaska");
        Country easternAustralia = new Country(41, "Eastern Australia");
        assertTrue(defaultCountries.contains(alaska));
        assertTrue(defaultCountries.contains(easternAustralia));
        Continent northAmerica = new Continent(0, "North America");
        Continent australia = new Continent(5, "Australia");
        assertTrue(defaultContinents.contains(northAmerica));
        assertTrue(defaultContinents.contains(australia));

        //Test that non existent countries or continents don't appear to be present
        Country nonExistentCountry = new Country(42, "Off The Map");
        Continent nonExistentContinent = new Continent(6, "Off The Map");
        assertFalse(defaultCountries.contains(nonExistentCountry));
        assertFalse(defaultContinents.contains(nonExistentContinent));
    }

    @Test
    public void correctConnectionsPresentInDefault(){

    }
}