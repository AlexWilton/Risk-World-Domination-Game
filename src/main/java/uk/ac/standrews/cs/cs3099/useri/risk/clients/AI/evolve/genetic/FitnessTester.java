package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.genetic;

import java.lang.reflect.InvocationTargetException;

/**
 * Interface for classes that can test fitness of genomes
 * 
 */
public interface FitnessTester {

	int testFitness(Genome genome) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException;
}
