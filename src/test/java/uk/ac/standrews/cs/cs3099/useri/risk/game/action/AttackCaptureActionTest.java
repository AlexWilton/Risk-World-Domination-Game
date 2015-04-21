package uk.ac.standrews.cs.cs3099.useri.risk.game.action;

import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.CLIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.RandomNumberGenerator;
import uk.ac.standrews.cs.cs3099.useri.risk.testHelper.TestGameStateFactory;

import static org.junit.Assert.*;

/**
 * Created by ryo_yanagida on 21/04/2015.
 */
public class AttackCaptureActionTest {

    State test;

    @Before
    public void Setup(){
        test = TestGameStateFactory.getTestGameState();
    }

    @Test
    public void testValidateAgainstState() throws Exception {
        AttackCaptureAction action = new AttackCaptureAction(test.getPlayer(1), 0, 0, 0);
        assertFalse(action.validateAgainstState(test));
    }

    @Test
    public void testPerformOnState() throws Exception {

    }
}