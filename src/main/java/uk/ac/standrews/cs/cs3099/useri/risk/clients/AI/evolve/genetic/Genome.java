package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.genetic;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

/**
 * represents one individual
 * 
 */
public class Genome {

	int fitness = 0;


	private LinkedList<Gene> genes;
	public static Random rand = new Random();

	public Genome() {
		genes = new LinkedList<Gene>();
	}

	public void mutate() {
		for (Gene g : genes)
			if (rand.nextDouble() <= EvolutionConstants.MUTATION_RATE)
				g.mutate();
	}


	public void addGene(Gene gene) {
		this.genes.addLast(gene);
	}

	public LinkedList<Gene> getGenes() {
		return this.genes;
	}

	public void randomValidInitialisation(Initialiser initialiser) {
		genes = initialiser.randomValidInitialisation();

	}

	public Genome clone() {
		Genome clone = new Genome();
		Gene g = null;
		for (Gene gene : genes) {
			g = gene.clone();
			clone.addGene(g);
		}
		clone.setFitness(fitness);
		return clone;
	}

	public void calculateFitness(FitnessTester tester)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		fitness = tester.testFitness(this);

	}

	public int getFitness() {
		return fitness;
	}

	private void setFitness(int fitness) {
		this.fitness = fitness;
	}

	public String toString() {
		String buf = "";
		buf += fitness + " ";
		for (Gene g : genes) {
			String[] classid = g.getClass().getName().split("\\.");
			buf += classid[classid.length - 1] + ", ";
		}
		return buf;
	}

	/**
	 * stores information of the genom in one string
	 * 
	 */
	public String toInformationString() {
		String buffer = "";
		for (Gene g : genes)
			buffer += g.toInformationString() + " ";
		return buffer;
	}

	/**
	 * inits the genome from the string
	 */
	public void initFromString(String in) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		Scanner s = new Scanner(in);
		s.useDelimiter(" ");
		while (s.hasNext()) {
			Gene g = EvolutionConstants.evolvedGene.getConstructor()
					.newInstance();
			g.initFromString(s.next());
			genes.add(g);
		}
		s.close();
	}

}
