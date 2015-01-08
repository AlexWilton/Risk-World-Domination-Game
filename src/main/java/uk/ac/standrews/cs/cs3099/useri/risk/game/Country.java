package uk.ac.standrews.cs.cs3099.useri.risk.game;

import java.util.ArrayList;

/**
 * represents a country with borders and keeps track of the troops currently deployed and the player that owns it.
 *
 */
public class Country {

	private String countryName;
	private ArrayList<Country> linkedCountries;
	private int troops;
	private Player owner;
	
}
