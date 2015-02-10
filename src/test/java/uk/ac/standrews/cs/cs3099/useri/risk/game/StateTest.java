package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.TestGameStateFactory;

import static org.junit.Assert.*;

public class StateTest {
    State testState;


    @Before
    public void setup(){
        testState = TestGameStateFactory.getTestGameState();
    }
    @Test
    public void detectNotWinCondTest(){
        assertFalse(testState.winConditionsMet());
    }
}
