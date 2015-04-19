package uk.ac.standrews.cs.cs3099.useri.risk.main;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.BulldogAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.BulldogAIv2Client;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.RandomAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.game.GameEngine;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.ClientSocketHandler;

import java.util.ArrayList;

/**
 * Created by patrick on 17/04/15.
 */
public class AIApp {


    public static void main(String argv[]){
        Client[] clients = {new BulldogAIv2Client(null),new RandomAIClient(null),new RandomAIClient(null),new RandomAIClient(null),new RandomAIClient(null),new RandomAIClient(null)};
        String[] names = {"bulldog","random","random","random","random","random"};
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i<clients.length; i ++){
            Thread t = new Thread(new AIRunner(clients[i],names[i]));
            t.start();
            threads.add(t);
        }

        for (Thread t : threads){
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }
}
