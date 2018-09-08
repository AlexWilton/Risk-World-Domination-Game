package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.genetic.Evolution;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.genetic.Gene;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.genetic.Genome;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * Evolves a CommandRaterAI generation, need to kill when needed, will save progress
 */
public class Evolver {

    public static void main (String argv[]){



        //init tools
        CommandRaterCrosser c = new CommandRaterCrosser();
        CommandRaterFitnessTester f = new CommandRaterFitnessTester();
        CommandRaterWeightSetInitialiser i = new CommandRaterWeightSetInitialiser();
        ArrayList<Genome> initial = new ArrayList<>();
        initial.add((new CommandRaterAIClient()).toWeightSet());


        try {
            //get from file, or from scratch if no file is supplied
            if (argv.length > 0)
                Evolution.initFromFile(argv[0],f,i,c);
            else
                Evolution.startEvolution(f, i, c, initial);


            while (true){
                System.out.println("Interpreting generation nr " + Evolution.getGenerationNumber());
                f.makeAllClients(Evolution.currentGeneration.getAllGenomes());
                System.out.println("Evolving in generation " + Evolution.getGenerationNumber());
                Evolution.advanceGeneration();
                System.out.println("done... write to file");
                //save it
                Evolution.writeToFile("CommandRater.evo");
            }

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
