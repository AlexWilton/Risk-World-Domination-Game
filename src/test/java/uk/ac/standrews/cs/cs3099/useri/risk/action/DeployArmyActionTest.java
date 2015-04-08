package uk.ac.standrews.cs.cs3099.useri.risk.action;

import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.cs3099.useri.risk.game.*;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DeployArmyActionTest{

    private State testState;
    private Country indonesia, newGuinea, westernAustralia, siam;
    private Player playerA, playerB;

    @Before
    public void setup(){
        testState = new State();
        ArrayList<Player> players = new ArrayList<Player>();
        playerA = new Player(0,null);
        playerB = new Player(1,null);
        players.add(playerA);
        players.add(playerB);

        testState.setup(new Map(), players);
        while(testState.getTurnStage() != TurnStage.STAGE_DEPLOYING) testState.nextStage(); //set turn stage to Deploy
        indonesia = testState.getCountryByID(38);
        newGuinea = testState.getCountryByID(39);
        westernAustralia = testState.getCountryByID(40);
        siam = testState.getCountryByID(37);

        //PlayerA has indonesia with 3 armies
        playerA.addCountry(indonesia);
        indonesia.setOwner(playerA);
        indonesia.setTroops(3);

        //PlayerB has WesternAustralia with 1 army
        playerB.addCountry(westernAustralia);
        westernAustralia.setOwner(playerB);
        westernAustralia.setTroops(1);
    }


    @Test
    public void testSuccessfulDeploy(){
        //Test that Player A successfully adds armies to Indonesia
        playerA.setUnassignedArmies(5);
        DeployArmyAction deploy = new DeployArmyAction(playerA, indonesia, 3);

        assertTrue(deploy.validateAgainstState(testState));

        deploy.performOnState(testState);

        assertEquals(6, indonesia.getTroops());
        assertEquals(2, playerA.getUnassignedArmies());
    }

    @Test
    public void testNotEnoughResources(){
        //Test that deployment fails when the player has less resources than he wants to deploy.
        playerA.setUnassignedArmies(2);

        DeployArmyAction deploy = new DeployArmyAction(playerA, indonesia, 3);

        assertFalse(deploy.validateAgainstState(testState));
    }

    @Test
    public void testNotYourCountry(){
        //Test that deployment fails when the player does not have the country they want to deploy to.
        playerA.setUnassignedArmies(4);

        DeployArmyAction deploy = new DeployArmyAction(playerA, westernAustralia, 3);

        assertFalse(deploy.validateAgainstState(testState));
    }

}