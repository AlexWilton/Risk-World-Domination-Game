package uk.ac.standrews.cs.cs3099.useri.risk.main;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.BulldogAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.RandomAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;

import java.util.ArrayList;
import java.util.HashMap;

public class AIApp {

    public static HashMap<String, Client> getAiClientEntries(){
        HashMap<String, Client> aiClients = new HashMap<>();
        aiClients.put("Bulldog v1", new BulldogAIClient(null));
        aiClients.put("Random2", new RandomAIClient(null));
        aiClients.put("Random1", new RandomAIClient(null));
        aiClients.put("Random", new RandomAIClient(null));
        return aiClients;
    }

    public static void main(String argv[]){
        HashMap<String, Client> aiClients = getAiClientEntries();
        String[] names = {"Bulldog v1","Random1","Random2","Random"};
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i<names.length; i ++){
            Thread t = new Thread(new AIRunner(aiClients.get(names[i]),names[i]));
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
