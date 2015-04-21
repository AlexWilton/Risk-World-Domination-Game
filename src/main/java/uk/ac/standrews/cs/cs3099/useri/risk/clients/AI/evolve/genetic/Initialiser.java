package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.genetic;

import java.util.LinkedList;

/**
 * provides an interface for initialising genomes
 * 
 */
public interface Initialiser {

	LinkedList<Gene> randomValidInitialisation();

}
