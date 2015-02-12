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
    public void winConditionsMetTest(){
        assertTrue(!detectNotWinCondTest()
                &&detectWinCondTest());
    }
    //test for non winning case
    @Test
    public boolean detectNotWinCondTest(){
        System.out.println("Not winning case:");
        return testState.winConditionsMet();
    }

    //test for detecting winning case
    @Test
    public boolean detectWinCondTest(){
        System.out.println("Winning case:");
        State winState = TestGameStateFactory.createWinCond();
        return winState.winConditionsMet();
    }
}
