package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.genetic;

/**
 * Interface for classes that can cross genomes
 * 
 */
public interface Crosser {
	Genome cross(Genome p1, Genome p2);

}
