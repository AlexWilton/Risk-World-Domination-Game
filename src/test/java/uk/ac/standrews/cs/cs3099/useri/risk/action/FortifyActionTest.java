package uk.ac.standrews.cs.cs3099.useri.risk.action;

import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.cs3099.useri.risk.game.*;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class FortifyActionTest {
    private State testState;
    private Country indonesia, newGuinea, westernAustralia;
    private Player playerA, playerB;

    @Before
    public void setUp() throws Exception {
        testState = new State();
        ArrayList<Player> players = new ArrayList<Player>();
        playerA = new Player(0,null);
        playerB = new Player(1,null);
        players.add(playerA);
        players.add(playerB);

        testState.setup(new Map(), players, null);
        while(testState.getTurnStage() != TurnStage.STAGE_FORTIFY) testState.nextStage(); //set turn stage to Fortify
        indonesia = testState.getCountryByID(38);
        newGuinea = testState.getCountryByID(39);
        westernAustralia = testState.getCountryByID(40);

        //PlayerA has indonesia with 3 armies
        playerA.addCountry(indonesia);
        indonesia.setOwner(playerA);
        indonesia.setTroops(3);

        //PlayerB has WesternAustralia with 1 army
        playerB.addCountry(westernAustralia);
        westernAustralia.setOwner(playerB);
        westernAustralia.setTroops(1);

        //PlayerA has NewGuinea with 1 army
        playerA.addCountry(newGuinea);
        newGuinea.setOwner(playerA);
        newGuinea.setTroops(1);
    }

    @Test
    public void testWantsToMoveToNotOwnedCountry() throws Exception {
        FortifyAction action = new FortifyAction(playerA, indonesia, westernAustralia, 2);

        assertFalse(action.validateAgainstState(testState));

    }

    @Test
    public void testWantsToMoveTooManyArmies() throws Exception {
        FortifyAction action = new FortifyAction(playerA, indonesia, newGuinea, 3);

        assertFalse(action.validateAgainstState(testState));

    }

    @Test
    public void testPerformOnState() throws Exception {
        FortifyAction action = new FortifyAction(playerA, indonesia, newGuinea, 2);

        assertTrue(action.validateAgainstState(testState));

        action.performOnState(testState);

        assertEquals(1, indonesia.getTroops());
        assertEquals(3, newGuinea.getTroops());
    }
}