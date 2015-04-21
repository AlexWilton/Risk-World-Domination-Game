package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.genetic;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * Performs the evolution, can save its state to a file and load from a file
 * 
 */
public class Evolution {
	public static Generation currentGeneration;

	public static void startEvolution(FitnessTester fitnessTester,
			Initialiser initialiser, Crosser crosser)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		currentGeneration = new Generation(fitnessTester, initialiser, crosser);
		currentGeneration.randomInit(EvolutionConstants.POPULATION_SIZE);
	}

    public static void startEvolution(FitnessTester fitnessTester,
                                      Initialiser initialiser, Crosser crosser, ArrayList<Genome> initialIndividuals)
            throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
        currentGeneration = new Generation(fitnessTester, initialiser, crosser,initialIndividuals);
        currentGeneration.randomInit(EvolutionConstants.POPULATION_SIZE);
    }

	/**
	 * go 1 step further for the evolution
	 */
	public static Genome advanceGeneration() throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		currentGeneration = currentGeneration.createFillialGeneration();
		return getFittestIndividuum();
	}

	public static Genome getFittestIndividuum() {
		return currentGeneration.getFittestIndividuum();
	}

	public static void writeToFile(String file) throws IOException {
		currentGeneration.writeToFile(file);
	}

	public static void initFromFile(String file, FitnessTester fitnessTester,
			Initialiser initialiser, Crosser crosser)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, IOException {
		currentGeneration = new Generation(file, fitnessTester, initialiser,
				crosser);
	}

	/**
	 * evolves for a number of generations returning the best individuum
	 */
	public static Genome evolveBestFit(int generations,
			FitnessTester fitnessTester, Initialiser initialiser,
			Crosser crosser) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		Generation generation = new Generation(fitnessTester, initialiser,
				crosser);
		generation.randomInit(EvolutionConstants.POPULATION_SIZE);
		for (int i = 0; i < generations; i++) {
			generation = generation.createFillialGeneration();
			System.out.println(i);
		}
		return generation.getFittestIndividuum();
	}

	public static int getGenerationNumber() {
		return currentGeneration.getGenerationNumber();
	}

}