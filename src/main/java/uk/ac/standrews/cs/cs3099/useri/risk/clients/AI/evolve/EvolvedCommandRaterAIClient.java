package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.AI;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.genetic.Genome;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by po26 on 21/04/15.
 */
public class EvolvedCommandRaterAIClient extends CommandRaterAIClient {

    public EvolvedCommandRaterAIClient(){
        super(getEvolvedGenome().getGenes());
    }

    public static Genome getEvolvedGenome(){
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader("CommandRater.evo"));
            r.readLine();
            Genome ret = new Genome();
            ret.initFromString(r.readLine());
            return ret;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
