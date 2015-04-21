package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.genetic.Crosser;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.genetic.Gene;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.genetic.Genome;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.RandomNumberGenerator;

import java.util.Random;

/**
 * Crosses two genomes representing a CommandRateAI
 */
public class CommandRaterCrosser implements Crosser {
    private static Random r = new Random();
    @Override
    public Genome cross(Genome p1, Genome p2) {
        Genome ret = new Genome();
        for (int i = 0; i < p1.getGenes().size();i++){
            if (r.nextDouble() < 0.5d)
                ret.addGene(p1.getGenes().get(i).clone());
            else
                ret.addGene(p1.getGenes().get(i).clone());

        }
        return ret;
    }
}
