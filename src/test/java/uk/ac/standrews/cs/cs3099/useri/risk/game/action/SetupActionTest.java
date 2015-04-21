package uk.ac.standrews.cs.cs3099.useri.risk.game.action;

import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Map;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.testHelper.TestGameStateFactory;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by ryo_yanagida on 21/04/2015.
 */
public class SetupActionTest {

    State testState;

    @Before
    public void setuo(){

        testState = TestGameStateFactory.unAssignedState();

    }


    @Test
    public void testValidateAgainstState() throws Exception {
        SetupAction sua = new SetupAction(
                testState.getPlayer(0),testState.getCountryByID(0));

        assertTrue(sua.validateAgainstState(testState));
    }

    @Test
    public void NotYourTurnTest() throws Exception {
        SetupAction sua = new SetupAction(
                testState.getPlayer(1),testState.getCountryByID(0));

        assertFalse(sua.validateAgainstState(testState));
    }

    @Test
    public void CountryTaken() throws Exception {
        testState.getCountryByID(0).setOwner(testState.getPlayer(0));

        SetupAction sua = new SetupAction(
                testState.getPlayer(0),testState.getCountryByID(0));

        assertFalse(sua.validateAgainstState(testState));
    }

    @Test
    public void CountryAssignedNotYours() throws Exception {
        State occupiedMapState = TestGameStateFactory.getTestGameState();

        occupiedMapState.setCurrentPlayer(0);
        occupiedMapState.getCountryByID(0).setOwner(occupiedMapState.getPlayer(0));
        SetupAction sua = new SetupAction(
                occupiedMapState.getPlayer(1),occupiedMapState.getCountryByID(0));

        occupiedMapState.setCurrentPlayer(1);
        assertFalse(sua.validateAgainstState(occupiedMapState));
    }

    @Test
    public void notEnoughArmy() throws Exception {
        State occupiedMapState = TestGameStateFactory.getTestGameState();
        occupiedMapState.getPlayer(1).setUnassignedArmies(0);
        occupiedMapState.setCurrentPlayer(1);
        SetupAction sua = new SetupAction(
                occupiedMapState.getPlayer(1),occupiedMapState.getCountryByID(1));
        assertFalse(sua.validateAgainstState(occupiedMapState));
    }

    @Test
    public void testPerformOnState() throws Exception {
        SetupAction sua = new SetupAction(
                testState.getPlayer(0),testState.getCountryByID(0));

        assertTrue(sua.validateAgainstState(testState));
        sua.performOnState(testState);
        assertTrue(testState.getCountryByID(0).getOwner()
                .equals(testState.getPlayer(0)));
    }


}