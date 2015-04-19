package uk.ac.standrews.cs.cs3099.useri.risk.main;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.BulldogAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.BulldogAIv2Client;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.RandomAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;

import java.util.ArrayList;
import java.util.HashMap;

public class AIApp {

    public static ArrayList<String> getListOfAvailableAIs(){
        ArrayList<String> aiClients = new ArrayList<>();
        aiClients.add("Bulldog v1");
        aiClients.add("Random2");
        aiClients.add("Random1");
        aiClients.add("Random");
        return aiClients;
    }

    /**
     * Creates a new AI client for a given AI type
     * @param name Type of AI
     * @return AI Client
     */
    public static Client createAiClient(String name){
        switch (name){
            case "Bulldog v1" : return new BulldogAIClient(null);
            case "Bulldog v2" : return new BulldogAIv2Client(null);
            case "Bulldog v3" : return new BulldogAIv3Client(null);
            case "Random" : return new RandomAIClient(null);
            default: return null;
        }
    }


    public static void main(String argv[]){
        String[] names = {"Bulldog v1","Random","Random","Random"};
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i<names.length; i ++){
            Thread t = new Thread(new AIRunner(createAiClient(names[i]),names[i]));
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
