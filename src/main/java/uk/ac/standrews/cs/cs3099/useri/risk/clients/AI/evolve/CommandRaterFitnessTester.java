package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.BulldogAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.ChihuahuaAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.GreatDaneAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.GreyhoundAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.genetic.FitnessTester;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.genetic.Genome;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.game.GameEngineLocal;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.HashMismatchException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by po26 on 21/04/15.
 */
public class CommandRaterFitnessTester implements FitnessTester {

    private Random r = new Random();

    private HashMap<Genome,CommandRaterAIClient> clients;
    private CommandRaterAIClient[] allClients;
    private Genome[] allGenomes;
    private HashMap<Genome,ArrayList<Integer>> previousRunScores;

    public void makeAllClients(LinkedList<Genome> genomes){
        System.out.println("making clients");
        clients = new HashMap<>();
        for (Genome g : genomes){
            clients.put(g,new CommandRaterAIClient(g.getGenes()));
        }

        allClients = clients.values().toArray(new CommandRaterAIClient[clients.values().size()]);
        allGenomes = clients.keySet().toArray(new Genome[clients.keySet().size()]);
        previousRunScores = new HashMap<>();

    }
    @Override
    public int testFitness(Genome genome) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        //play 4 random games
        System.out.println("testing genome " +genome.toInformationString());

        int fitness = 0;
        int prevRuns = 0;

        if (previousRunScores.containsKey(genome)){
            System.out.println("found previous scores!");
            ArrayList<Integer> prevScores = previousRunScores.get(genome);
            for (int i = 0;i < 12 && i < prevScores.size();i++){
                prevRuns++;
                fitness += prevScores.get(i);
            }
        }

        for (int i = prevRuns ; i < 12; i++){
            try {
                ArrayList<Genome> usedGenomes = new ArrayList<>();
                usedGenomes.add(genome);
                while (usedGenomes.size() < 6) {
                    int randNr = r.nextInt(clients.keySet().size());
                    while (usedGenomes.contains(allGenomes[randNr])) {
                        randNr = r.nextInt(clients.keySet().size());
                    }
                    Genome rand = allGenomes[randNr];
                    usedGenomes.add(rand);

                }

                ArrayList<Client> c = new ArrayList<>();

                int y = 0;
                for (Genome g : usedGenomes){
                    CommandRaterAIClient client = clients.get(g);
                    client.setPlayerId(y++);
                    client.reset();
                    c.add(client);
                }





                GameEngineLocal g = new GameEngineLocal();
                g.initialise(c);
                g.run(6000);
                fitness += g.getPlayerPoints(0);

                for (int j = 1; j < usedGenomes.size(); j++){
                    if (previousRunScores.containsKey(usedGenomes.get(j))){
                        ArrayList<Integer> prevScores = previousRunScores.get(usedGenomes.get(j));
                        prevScores.add(g.getPlayerPoints(j));
                    }
                    else {
                        ArrayList<Integer> prevScores = new ArrayList<>();
                        prevScores.add(g.getPlayerPoints(j));
                        previousRunScores.put(usedGenomes.get(j),prevScores);
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e){
                i--;
                e.printStackTrace();
            }
        }

        //factor in environment
        //play game against everyone and multiply by square root of score


            while (true){
                try{
                ArrayList<Client> c = new ArrayList<>();
                c.add(clients.get(genome));
                c.get(0).setPlayerId(0);
                    ((CommandRaterAIClient) c.get(0)).reset();
                c.add(new BulldogAIClient(null));
                c.get(1).setPlayerId(1);
                c.add(new ChihuahuaAIClient(null));
                c.get(2).setPlayerId(2);
                c.add(new GreatDaneAIClient(null));
                c.get(3).setPlayerId(3);
                c.add(new GreyhoundAIClient(null));
                c.get(4).setPlayerId(4);
                c.add(new CommandRaterAIClient());
                c.get(5).setPlayerId(5);
                GameEngineLocal g = new GameEngineLocal();
                g.initialise(c);
                g.run(6000);

                int multiplier = (int) Math.sqrt(g.getPlayerPoints(0));


                return fitness*multiplier;
                } catch (Exception e){
                    System.out.println("error in game, try again");
                }
            }


    }
}
