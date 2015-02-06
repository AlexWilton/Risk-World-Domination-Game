package uk.ac.standrews.cs.cs3099.useri.risk.action;


import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.cs3099.useri.risk.game.*;

import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class AttackActionTest{

    private State testState;
    private CountrySet countries;
    private Country indonesia, newGuinea, westernAustralia, easternAustralia;
    private Player playerA, playerB;

    @Before
    public void setup(){
        testState = new State();


        ArrayList<Player> players = new ArrayList<Player>();
        playerA = new Player(0,null);
        playerB = new Player(1,null);
        players.add(playerA);
        players.add(playerB);
        testState.setup(new Map(), players, null);
        while(testState.getTurnStage() != TurnStage.STAGE_BATTLES) testState.nextStage(); //set turn stage to Battle
        indonesia = testState.getCountryByID(38);
        newGuinea = testState.getCountryByID(39);
        westernAustralia = testState.getCountryByID(40);
        easternAustralia = testState.getCountryByID(41);

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
    }


    @Test
    public void testSuccessfulAttack(){
        //Test that Player A successfully conquers westernAustralia from Player B
        int[] playerADice = {6,6};
        int[] playerBDice = {1};
        AttackAction attack = new AttackAction(playerA, indonesia, westernAustralia, playerADice, playerBDice);

        assertTrue(attack.validateAgainstState(testState));

        attack.performOnState(testState);

        assertTrue(westernAustralia.getOwner().equals(playerA));
        assertTrue(playerA.getOccupiedCountries().contains(westernAustralia));
        assertFalse(playerB.getOccupiedCountries().contains(westernAustralia));
        assertTrue(westernAustralia.getTroops() == 2);
        assertTrue(indonesia.getTroops() == 1);
    }

    @Test
    public void testUnSuccessfulAttack(){
        //Test that Player A loses one army in indonesia when failing to take westernAustralia
        int[] playerADice = {3,3};
        int[] playerBDice = {3};
        AttackAction attack = new AttackAction(playerA, indonesia, westernAustralia, playerADice, playerBDice);

        assertTrue(attack.validateAgainstState(testState));

        attack.performOnState(testState);

        assertTrue(westernAustralia.getOwner().equals(playerB));
        assertTrue(playerB.getOccupiedCountries().contains(westernAustralia));
        assertFalse(playerA.getOccupiedCountries().contains(westernAustralia));
        assertTrue(westernAustralia.getTroops() == 1);
        assertTrue(indonesia.getTroops() == 2);
    }

    @Test
    public void attemptInvalidMoves(){
        int[] playerADice = {6,6,6}; // Attempt to use 3 dice. (PlayerA can only attack with a maximum of 2 armies!)
        int[] playerBDice = {5};
        AttackAction attack = new AttackAction(playerA, indonesia, westernAustralia, playerADice, playerBDice);
        assertFalse(attack.validateAgainstState(testState));

        playerADice = new int[]{6,6};
        playerBDice = new int[]{}; //Player B defends with no dice. (Must defend with at least one army)
        attack = new AttackAction(playerA, indonesia, westernAustralia, playerADice, playerBDice);
        assertFalse(attack.validateAgainstState(testState));

        //playerA cannot attack as he must leave his army in newGuinea and he only has one troop there.
        playerADice = new int[]{6};
        playerBDice = new int[]{6};
        attack = new AttackAction(playerA, newGuinea, westernAustralia, playerADice, playerBDice);
        assertFalse(attack.validateAgainstState(testState));
    }

}