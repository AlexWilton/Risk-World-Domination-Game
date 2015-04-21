package uk.ac.standrews.cs.cs3099.useri.risk.main;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.BulldogAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.ChihuahuaAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.GreatDaneAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.GreyhoundAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.CommandRaterAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.EvolvedCommandRaterAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.game.GameEngineLocal;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by po26 on 21/04/15.
 */
public class AIAnalyser {

    static int playLocalGame(ArrayList<Client> clients){
        GameEngineLocal g = new GameEngineLocal();
        g.initialise(clients);
        g.run(3000,20);

        return g.getWinner();
    }

    public static void main (String argv[]) {

        HashMap<Integer,ArrayList<Integer>> playerPlaces = new HashMap<>();
        ArrayList<String> p = new ArrayList<>();
        p.add("Bulldog");
        p.add("Greyhound");
        p.add("GreatDane");
        p.add("Chihuahua");
        p.add("CommandRater");
        p.add("CommandRaterEvolved");

        for (int i = 0; i<p.size(); i++){
            playerPlaces.put(i,new ArrayList<Integer>());
        }

        for (int i = 0; i < 100; i++){
            ArrayList<Client> c = new ArrayList<>();




            for(int j = 0; j< p.size(); j++){
                c.add(AIApp.createAiClient(p.get(j)));
                c.get(j).setPlayerId(j);
            }

            GameEngineLocal g = new GameEngineLocal();
            g.initialise(c);
            g.run(3000, 20);
            for(int j = 0; j< p.size(); j++){
                playerPlaces.get(j).add(g.getPlayerRank(j));
            }
        }

        try {
            PrintWriter w = new PrintWriter(new FileWriter("analysis.csv"));
            w.println("player, 1st places, 2nd places, 3rd places, 4th places, 5th places, 6th places");
            for (int i : playerPlaces.keySet()){
                int[] places = new int[6];
                for (int j : playerPlaces.get(i)){
                    places[j-1] ++;
                }
                w.println(p.get(i) + "," + places[0] +"," + places[1] +"," + places[2] +"," + places[3] +"," + places[4] +"," + places[5]);
            }
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
