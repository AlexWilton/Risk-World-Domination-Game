package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.genetic;



/**
 * Contains constants relevant for the evolution
 * 
 */
public abstract class EvolutionConstants {

	public static final int POPULATION_SIZE = 200;
	public static final double MUTATION_RATE = 0.05d;
	public static final double ELITE_PRESERVED = 0.10d;
	public static final double FRESH_BLOOD = 0.10d;
	public static final double PARENT_RATE = 0.20d;
	public static final double MUTATANT_ELITE = 0.15d;
	public static final double MUTATANT_CHILDREN = 0.10d;
	public static final int MAX_TIME_SC = 1800;
	public static final Class<? extends Gene> evolvedGene = null;//WeightSet.class;

}
