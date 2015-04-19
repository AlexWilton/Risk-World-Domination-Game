package uk.ac.standrews.cs.cs3099.useri.risk.main;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.BulldogAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.GreatDaneAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.ChihuahuaAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.GreyhoundAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;

import java.util.ArrayList;
import java.util.HashMap;

public class AIApp {

    public static HashMap<String, Client> getAiClientEntries(){
        HashMap<String, Client> aiClients = new HashMap<>();
        aiClients.put("GreatDane", new GreatDaneAIClient(null));
        aiClients.put("Greyhound", new GreyhoundAIClient(null));
        aiClients.put("Bulldog", new BulldogAIClient(null));
        aiClients.put("Chihuahua", new ChihuahuaAIClient(null));
        return aiClients;
    }

    public static void main(String argv[]){
        HashMap<String, Client> aiClients = getAiClientEntries();
        String[] names = {"GreatDane","Greyhound","Bulldog","Chihuahua"};
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
