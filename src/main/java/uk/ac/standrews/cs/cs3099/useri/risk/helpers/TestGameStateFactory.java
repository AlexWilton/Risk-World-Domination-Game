package uk.ac.standrews.cs.cs3099.useri.risk.helpers;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.CLIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.game.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by po26 on 03/02/15.
 */
public class TestGameStateFactory {

    public static State getTestGameState(){

        //create gamestate
        State gamestate = new State();

        //initialise map
        Map map = new Map();

        //setup a few risk cards in the deck
        ArrayList<RiskCard> riskCards = new ArrayList<RiskCard>();
        riskCards.add(new RiskCard(RiskCardType.TYPE_ARTILLERY,0));
        riskCards.add(new RiskCard(RiskCardType.TYPE_ARTILLERY,1));
        riskCards.add(new RiskCard(RiskCardType.TYPE_CAVALRY,2));
        riskCards.add(new RiskCard(RiskCardType.TYPE_CAVALRY,3));
        riskCards.add(new RiskCard(RiskCardType.TYPE_ARTILLERY,4));
        riskCards.add(new RiskCard(RiskCardType.TYPE_INFANTRY,5));
        riskCards.add(new RiskCard(RiskCardType.TYPE_INFANTRY,6));

        //setup two cliclients
        CLIClient c0 = new CLIClient(gamestate);
        CLIClient c1 = new CLIClient(gamestate);

        //setup two players, just assign all the countries alternating with two armies in each
        ArrayList<Player> players = new ArrayList<Player>();
        players.add(new Player(0,c0));
        players.add(new Player(1,c1));

        //go through all countries
        Iterator<Player> p = players.iterator();
        int tc =1;

        for (Country c : map.getAllCountries()){
            if (!p.hasNext()){
                p = players.iterator();
            }
            if (tc > 3) {
                tc = 1;
            }
            p.next().occupyCountry(c, tc++);
        }

        gamestate.setup(map,players,riskCards);

        return gamestate;
    }

    //make winning condition
    public static State createWinCond(){
         //create gamestate
        State gamestate = new State();

        //initialise map
        Map map = new Map();

        //setup a few risk cards in the deck
        ArrayList<RiskCard> riskCards = new ArrayList<RiskCard>();
        riskCards.add(new RiskCard(RiskCardType.TYPE_ARTILLERY,0));
        riskCards.add(new RiskCard(RiskCardType.TYPE_ARTILLERY,1));
        riskCards.add(new RiskCard(RiskCardType.TYPE_CAVALRY,2));
        riskCards.add(new RiskCard(RiskCardType.TYPE_CAVALRY,3));
        riskCards.add(new RiskCard(RiskCardType.TYPE_ARTILLERY,4));
        riskCards.add(new RiskCard(RiskCardType.TYPE_INFANTRY,5));
        riskCards.add(new RiskCard(RiskCardType.TYPE_INFANTRY,6));

        //setup two cliclients
        CLIClient c0 = new CLIClient(gamestate);
        CLIClient c1 = new CLIClient(gamestate);

        //setup two players, just assign all the countries alternating with two armies in each
        ArrayList<Player> players = new ArrayList<Player>();
        players.add(new Player(0,c0));
        players.add(new Player(1,c1));

        //go through all countries
        Iterator<Player> p = players.iterator();
        int tc =1;

        for (Country c : map.getAllCountries()){
            Player testPlayer = players.get(0);
            testPlayer.occupyCountry(c, tc++);
        }

        gamestate.setup(map,players,riskCards);

        return gamestate;
    }
}
