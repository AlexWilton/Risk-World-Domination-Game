package uk.ac.standrews.cs.cs3099.useri.risk.game.action;

import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.CLIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.game.TurnStage;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.CountrySet;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Map;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.RandomNumberGenerator;
import uk.ac.standrews.cs.cs3099.useri.risk.testHelper.TestGameStateFactory;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by ryo_yanagida on 21/04/2015.
 */
public class AttackCaptureActionTest {

    State test;

    private State testState;
    private CountrySet countries;
    private Country indonesia, newGuinea, westernAustralia, siam;
    private Player playerA, playerB;

    @Before
    public void Setup(){
        test = TestGameStateFactory.getTestGameState();

        testState = new State();


        ArrayList<Player> players = new ArrayList<Player>();
        playerA = new Player(0,null);
        playerB = new Player(1,null);
        players.add(playerA);
        players.add(playerB);
        testState.setup(new Map(), players);
        while(testState.getTurnStage() != TurnStage.STAGE_BATTLES) testState.nextStage(); //set turn stage to Battle
        indonesia = testState.getCountryByID(38);
        newGuinea = testState.getCountryByID(39);
        westernAustralia = testState.getCountryByID(40);
        siam = testState.getCountryByID(37);

        //PlayerA has indonesia with 3 armies and PlayerB has WesternAustralia with 1 army
        playerA.addCountry(indonesia);
        indonesia.setOwner(playerA);
        indonesia.setTroops(3);
        playerB.addCountry(westernAustralia);
        westernAustralia.setOwner(playerB);
        westernAustralia.setTroops(1);

        //PlayerA has NewGuinea with 1 army
        playerA.addCountry(newGuinea);
        newGuinea.setOwner(playerA);
        newGuinea.setTroops(1);

        //PlayerB has Siam with 2 armies
        playerB.addCountry(siam);
        siam.setTroops(2);
        siam.setOwner(playerB);

    }

    @Test
    public void testValidateAgainstState() throws Exception {
        AttackCaptureAction action = new AttackCaptureAction(test.getPlayer(1), 0, 0, 0);
        assertFalse(action.validateAgainstState(test));

        //Successful attacks:
        int[] playerADice = {6,6};
        int[] playerBDice = {1};
        AttackAction attack = new AttackAction(playerA, indonesia, westernAustralia, playerADice, playerBDice);
        assertTrue(attack.validateAgainstState(testState));
        attack.performOnState(testState);

        AttackCaptureAction aca =
                new AttackCaptureAction(
                        playerA,
                        indonesia.getCountryId(),
                        westernAustralia.getCountryId(),
                        indonesia.getTroops()-1);

        assertTrue(aca.validateAgainstState(testState));

        testState.setCurrentPlayer(playerB.getID());
        AttackCaptureAction acF1 =
                new AttackCaptureAction(
                        playerB,
                        indonesia.getCountryId(),
                        westernAustralia.getCountryId(),
                        indonesia.getTroops()-1);
        assertFalse(acF1.validateAgainstState(testState));

        testState.getCountryByID(indonesia.getCountryId()).setOwner(playerB);
        AttackCaptureAction acF2 =
                new AttackCaptureAction(
                        playerA,
                        indonesia.getCountryId(),
                        westernAustralia.getCountryId(),
                        indonesia.getTroops()-1);
        assertFalse(acF2.validateAgainstState(testState));

        testState.getCountryByID(westernAustralia.getCountryId()).setOwner(playerA);

        AttackCaptureAction acF3 =
                new AttackCaptureAction(
                        playerB,
                        indonesia.getCountryId(),
                        westernAustralia.getCountryId(),
                        indonesia.getTroops()-1);

        assertFalse(acF3.validateAgainstState(testState));

        AttackCaptureAction acF4 =
                new AttackCaptureAction(
                        playerA,
                        indonesia.getCountryId(),
                        westernAustralia.getCountryId(),
                        indonesia.getTroops());
        assertFalse(acF4.validateAgainstState(testState));

    }

    @Test
    public void testPerformOnState() throws Exception {
        AttackCaptureAction action = new AttackCaptureAction(test.getPlayer(1), 0, 0, 0);
        assertFalse(action.validateAgainstState(test));

        //Successful attacks:
        int[] playerADice = {6,6};
        int[] playerBDice = {1};
        AttackAction attack = new AttackAction(playerA, indonesia, westernAustralia, playerADice, playerBDice);
        assertTrue(attack.validateAgainstState(testState));
        attack.performOnState(testState);

        AttackCaptureAction aca =
                new AttackCaptureAction(
                        playerA,
                        indonesia.getCountryId(),
                        westernAustralia.getCountryId(),
                        indonesia.getTroops()-1);

        assertTrue(aca.validateAgainstState(testState));
        aca.performOnState(testState);


    }
}