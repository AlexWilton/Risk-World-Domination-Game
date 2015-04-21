package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve;

import com.sun.org.apache.xml.internal.security.Init;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.genetic.Gene;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.genetic.Initialiser;

import java.util.LinkedList;
import java.util.Random;

/**
 * Created by po26 on 20/04/15.
 */
public class CommandRaterWeightSetInitialiser implements Initialiser {
    

    @Override
    public LinkedList<Gene> randomValidInitialisation() {

        LinkedList<Gene> ret = new LinkedList<>();

        for (int i = 0; i < 15; i++){
            ret.add(new MultiplierGene());
        }

        ret.add(new ConstantVarGene());

        return ret;
    }
}
