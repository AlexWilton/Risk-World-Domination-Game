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
        indonesia = testState.getCountryByID(38);
        newGuinea = testState.getCountryByID(39);
        westernAustralia = testState.getCountryByID(40);
        easternAustralia = testState.getCountryByID(41);
    }


    @Test
    public void testAttackAction(){
        //Test that Player A successfully conquers westernAustralia from Player B
        playerA.addCountry(indonesia);
        indonesia.setOwner(playerA);
        indonesia.setTroops(3);
        playerB.addCountry(westernAustralia);
        westernAustralia.setOwner(playerB);
        westernAustralia.setTroops(1);

        int[] playerADice = {6,6};
        int[] playerBDice = {1};
        AttackAction attack = new AttackAction(playerA, indonesia, westernAustralia, playerADice, playerBDice);
        attack.performOnState(testState);

        assertTrue(westernAustralia.getOwner().equals(playerA));
        assertTrue(playerA.getOccupiedCountries().contains(westernAustralia));
        assertFalse(playerB.getOccupiedCountries().contains(westernAustralia));
        assertTrue(westernAustralia.getTroops() == 2);
        assertTrue(indonesia.getTroops() == 1);

    }
}