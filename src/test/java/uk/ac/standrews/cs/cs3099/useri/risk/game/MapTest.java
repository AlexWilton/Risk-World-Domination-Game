package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.*;

import java.util.ArrayList;
import java.util.Stack;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;



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
        //check Alaska(0)-Kamichatka(29) connection exists both ways
        Country alaska = defaultCountries.get(0);
        assertTrue(alaska.getNeighbours().contains(new Country(29)));
        Country kamichatka = defaultCountries.get(29);
        assertTrue(kamichatka.getNeighbours().contains(new Country(0)));


        //check Madagaskar(25)-Great Britain(16) doesn't exist
        Country madagaskar = defaultCountries.get(25);
        assertFalse(madagaskar.getNeighbours().contains(new Country(16)));
        Country gb = defaultCountries.get(16);
        assertFalse(gb.getNeighbours().contains(25));

        //check continents contains correct countries:
        Continent northAmerica = defaultContinents.get(0);
        CountrySet northernAmerica = northAmerica.getCountries();
        ArrayList<Integer> idlist = northernAmerica.getIDList();
        assertTrue(idlist.contains(0)
                && idlist.contains(1)
            && idlist.contains(2)
            && idlist.contains(3)
            && idlist.contains(4)
            && idlist.contains(5)
            && idlist.contains(6)
            && idlist.contains(7)
            && idlist.contains(8));
    }

    @Test
    public void testCardParsing(){
        Stack<RiskCard> riskCards = defaultMap.getCardsFromMapData();
        assertTrue(riskCards.size()==44);
        assertTrue(riskCards.get(43).getType()==RiskCardType.TYPE_WILDCARD);
        System.out.println(riskCards.get(43).getCardID());
        assertTrue(riskCards.peek().getCardID()==43);
        assertFalse(riskCards.isEmpty());
    }


}