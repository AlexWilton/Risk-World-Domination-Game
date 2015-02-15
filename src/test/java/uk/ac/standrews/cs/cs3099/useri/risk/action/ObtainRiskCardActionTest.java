package uk.ac.standrews.cs.cs3099.useri.risk.action;

import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.cs3099.useri.risk.game.*;

import java.util.ArrayList;
import java.util.Stack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ObtainRiskCardActionTest {
    private State testState;
    private Country indonesia, newGuinea, westernAustralia, siam;
    private Player playerA, playerB;

    @Before
    public void setup(){
        testState = new State();

        Stack<RiskCard> riskCards = new Stack<RiskCard>();
        riskCards.add(new RiskCard(RiskCardType.TYPE_ARTILLERY,0));
        riskCards.add(new RiskCard(RiskCardType.TYPE_ARTILLERY,1));
        riskCards.add(new RiskCard(RiskCardType.TYPE_CAVALRY,2));
        riskCards.add(new RiskCard(RiskCardType.TYPE_CAVALRY,3));
        riskCards.add(new RiskCard(RiskCardType.TYPE_ARTILLERY,4));
        riskCards.add(new RiskCard(RiskCardType.TYPE_INFANTRY,5));
        riskCards.add(new RiskCard(RiskCardType.TYPE_INFANTRY,6));

        ArrayList<Player> players = new ArrayList<Player>();
        playerA = new Player(0,null);
        players.add(playerA);

        testState.setup(new Map(), players, riskCards);
        while(testState.getTurnStage() != TurnStage.STAGE_GET_CARD) testState.nextStage(); //set turn stage to Deploy

    }

    @Test
    public void testValidateAgainstState() throws Exception {
        ObtainRiskCardAction action = new ObtainRiskCardAction(playerA);
        // has to fail, no battle won.
        assertFalse(action.validateAgainstState(testState));

        testState.winning();
        assertTrue(action.validateAgainstState(testState));

    }

    @Test
    public void testPerformOnState() throws Exception {
        ObtainRiskCardAction action = new ObtainRiskCardAction(playerA);

        testState.winning();
        assertTrue(action.validateAgainstState(testState));
        action.performOnState(testState);
        assertEquals(1, playerA.getCards().size());
        assertEquals(0, playerA.getCards().get(0).getCardID());

    }
}