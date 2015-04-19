package uk.ac.standrews.cs.cs3099.useri.risk.main;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.BulldogAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.GreatDaneAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.game.GameEngineLocal;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.RandomNumberGenerator;

import java.util.ArrayList;

/**
 * Created by patrick on 19/04/15.
 */
public class LocalRun {

    static int playLocalGame(ArrayList<Client> clients){
        GameEngineLocal g = new GameEngineLocal();
        g.initialise(clients);
        g.run();

        return g.getWinner();
    }

    public static void main (String [] argv){
        ArrayList<Client> c = new ArrayList<>();
        c.add(new GreatDaneAIClient(null));
        c.add(new GreatDaneAIClient(null));
        c.add(new GreatDaneAIClient(null));
        c.add(new GreatDaneAIClient(null));
        c.add(new GreatDaneAIClient(null));
        c.add(new GreatDaneAIClient(null));
        c.get(0).setPlayerId(0);
        c.get(1).setPlayerId(1);
        c.get(2).setPlayerId(2);
        c.get(3).setPlayerId(3);
        c.get(4).setPlayerId(4);
        c.get(5).setPlayerId(5);

        System.out.println(playLocalGame(c));

    }
}
