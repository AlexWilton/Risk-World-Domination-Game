package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.genetic;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.CommandRaterFitnessTester;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.RatedCommand;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * holds one generation of the evolution and can create fillial generations
 * has a function for storing to file
 *
 */
/**
 * @author bs44
 * 
 */
public class Generation {

	private LinkedList<Genome> individuals;

	private FitnessTester fitnessTester;
	private Initialiser initialiser;
	private Crosser crosser;
	private int generationNumber;

	/**
	 * writes this generation to a file
	 */
	public void writeToFile(String file) throws IOException {
		PrintWriter out = new PrintWriter(new FileWriter(file));
		out.println(generationNumber);
		for (Genome g : individuals) {
			out.println(g.toInformationString());
		}
		out.close();

	}

	/**
	 * Constructor, takes the testers for this generation, inits it from a file
	 */
	public Generation(String file, FitnessTester fitnessTester,
			Initialiser initialiser, Crosser crosser)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, IOException {
		this.crosser = crosser;
		this.initialiser = initialiser;
		this.fitnessTester = fitnessTester;
		individuals = new LinkedList<Genome>();
		BufferedReader in = new BufferedReader(new FileReader(file));
		String numberBuffer = in.readLine();
		generationNumber = Integer.parseInt(numberBuffer);
		while (in.ready()) {
			Genome g = new Genome();
			g.initFromString(in.readLine());
			individuals.add(g);
		}
		in.close();
	}

	/**
	 * Constructor, creates an empty generation
	 */
	public Generation(FitnessTester fitnessTester, Initialiser initialiser,
			Crosser crosser) {
		this.crosser = crosser;
		this.initialiser = initialiser;
		this.fitnessTester = fitnessTester;
		generationNumber = 0;
		individuals = new LinkedList<Genome>();
	}

    /**
     * Constructor, creates an empty generation and adds the initially contained genome
     */
    public Generation(FitnessTester fitnessTester, Initialiser initialiser,
                       Crosser crosser, ArrayList<Genome> initialGenomes) {
        this.crosser = crosser;
        this.initialiser = initialiser;
        this.fitnessTester = fitnessTester;
        generationNumber = 0;
        individuals = new LinkedList<>();
        individuals.addAll(initialGenomes);
    }

	/**
	 * initialises a generation at random
	 */
	public void randomInit(int populationSize) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		for (int i = individuals.size(); i < populationSize; i++) {
			Genome temp = new Genome();
			temp.randomValidInitialisation(initialiser);
			individuals.add(temp);
		}
        ((CommandRaterFitnessTester) fitnessTester).makeAllClients(individuals);
		sortForFitness();

	}

	/**
	 * Creates an offspring genration
	 */
	public Generation createFillialGeneration() throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		LinkedList<Genome> newIndividuals = new LinkedList<Genome>();
		// Add fresh blood
		for (int i = 0; i < individuals.size() * EvolutionConstants.FRESH_BLOOD; i++) {
			Genome temp = new Genome();
			temp.randomValidInitialisation(initialiser);
			newIndividuals.add(temp);
		}
		// preserve Elite
		newIndividuals
				.addAll(individuals.subList(
						0,
						(int) (individuals.size() * EvolutionConstants.ELITE_PRESERVED)));
		// mutate Elite
		LinkedList<Genome> mutants = new LinkedList<Genome>();
		mutants.addAll(individuals.subList(0,
				(int) (individuals.size() * EvolutionConstants.MUTATANT_ELITE)));
		for (int i = 0; i < mutants.size(); i++) {
			mutants.set(i, mutants.get(i).clone());
			mutants.get(i).mutate();
		}
		newIndividuals.addAll(mutants);
		// Deduce Parent subgroup
		ArrayList<Genome> parents = new ArrayList<Genome>(individuals.subList(
				0, (int) (individuals.size() * EvolutionConstants.PARENT_RATE)));
		// Children mutants
		LinkedList<Genome> childrenMutants = new LinkedList<Genome>();
		for (int i = 0; i < (int) (individuals.size() * EvolutionConstants.MUTATANT_CHILDREN); i++) {
			Genome child = crosser.cross(
					parents.get((int) (Math.random() * parents.size())),
					parents.get((int) (Math.random() * parents.size())));
			child.mutate();
			childrenMutants.add(child);
		}
		newIndividuals.addAll(childrenMutants);
		// Add children
		while (newIndividuals.size() < individuals.size()) {
			Genome child = crosser.cross(
					parents.get((int) (Math.random() * parents.size())),
					parents.get((int) (Math.random() * parents.size())));
			newIndividuals.add(child);
		}
		Generation fillial = new Generation(this.fitnessTester,
				this.initialiser, this.crosser);
		fillial.setIndividuals(newIndividuals);
		fillial.sortForFitness();
		fillial.generationNumber = generationNumber + 1;
		return fillial;
	}

	public Genome getFittestIndividuum() {
		return individuals.getFirst();
	}

	public void setIndividuals(LinkedList<Genome> individuals) {
		this.individuals = individuals;
	}

	/**
	 * calculates fitness of individuals and sorts them
	 */
	private void sortForFitness() throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
        ((CommandRaterFitnessTester) fitnessTester).makeAllClients(individuals);
		for (Genome g : individuals)
			g.calculateFitness(fitnessTester);
		Comparator<Genome> comp = new FitnessComparator();
		Collections.sort(individuals, comp);
	}

	public int getGenerationNumber() {
		return this.generationNumber;
	}

    public LinkedList<Genome> getAllGenomes() {
        return individuals;
    }

}
