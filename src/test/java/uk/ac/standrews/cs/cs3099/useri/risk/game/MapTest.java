package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.junit.Test;
import static org.junit.Assert.*;



public class MapTest {
    @Test
    public void canLoadDefaultMap(){
        Map map = new Map();
        assertTrue(map.getContinents() != null);
    }
}