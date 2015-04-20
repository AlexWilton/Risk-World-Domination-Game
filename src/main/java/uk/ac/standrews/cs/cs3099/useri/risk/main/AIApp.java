package uk.ac.standrews.cs.cs3099.useri.risk.main;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.BulldogAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.ChihuahuaAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.GreatDaneAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.GreyhoundAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;

import java.util.ArrayList;


public class AIApp {

    public static ArrayList<String> getListOfAvailableAIs(){
        ArrayList<String> aiClients = new ArrayList<>();
        aiClients.add("Bulldog");
        aiClients.add("Greyhound");
        aiClients.add("GreatDane");
        aiClients.add("Chihuahua");
        return aiClients;
    }

    /**
     * Creates a new AI client for a given AI type
     * @param name Type of AI
     * @return AI Client
     */
    public static Client createAiClient(String name){
        switch (name){
            case "Bulldog" : return new BulldogAIClient(null);
            case "Greyhound" : return new GreyhoundAIClient(null);
            case "GreatDane" : return new GreatDaneAIClient(null);
            case "Chihuahua" : return new ChihuahuaAIClient(null);
            default: return null;
        }
    }


    public static void main(String argv[]){
        String[] names = {"Bulldog","Greyhound","GreatDane","Chihuahua"};
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
