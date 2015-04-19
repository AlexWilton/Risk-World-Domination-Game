package uk.ac.standrews.cs.cs3099.useri.risk.main;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.BulldogAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.BulldogAIv2Client;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.BulldogAIv3Client;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.RandomAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.game.GameEngine;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.ClientSocketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class AIApp {

    public static HashMap<String, Client> getAiClientEntries(){
        HashMap<String, Client> aiClients = new HashMap<>();
        aiClients.put("Bulldog v1", new BulldogAIClient(null));
        aiClients.put("Bulldog v2", new BulldogAIv2Client(null));
        aiClients.put("Bulldog v3", new BulldogAIv3Client(null));
        aiClients.put("Random", new RandomAIClient(null));
        return aiClients;
    }

    public static void main(String argv[]){
        HashMap<String, Client> aiClients = getAiClientEntries();
        String[] names = {"Bulldog v2","Random","Random","Random","Random","Random"};
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i<aiClients.size(); i ++){
            Thread t = new Thread(new AIRunner(aiClients.get(names[i]),names[i]));
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
