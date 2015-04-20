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
        while(c.size()<6) {
            c.add(new GreatDaneAIClient(null));
        }

        for(int i = 0; i< c.size(); i++){
           c.get(i).setPlayerId(i);
        }

        System.out.println(playLocalGame(c));

    }
}
