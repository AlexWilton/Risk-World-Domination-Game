package uk.ac.standrews.cs.cs3099.useri.risk.game;

import java.util.ArrayList;

/**
 * represents a player in the game, and keeps track of the occupied countries and risk cards
 *
 */
public class Player {
	private ArrayList<Country> occupiedCountries;
	private ArrayList<RiskCard> cards;
	private int unassignedArmy;
	///enable if a player is disconnected to skip his turn
	private boolean inactive;
}
