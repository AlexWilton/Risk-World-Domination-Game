package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.genetic;

import java.util.Comparator;

/**
 * used for comparing and sorting genomes for fitness
 * 
 */
public class FitnessComparator implements Comparator<Genome> {
	public int compare(Genome a, Genome b) {
		return b.getFitness() - a.getFitness();
	}

}
