package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.genetic;
/**
 * Parts to be evolved have to implement this interface
 * 
 */
public interface Gene {
	void mutate();

	Gene clone();

	String toInformationString();

	void initFromString(String in);
}
