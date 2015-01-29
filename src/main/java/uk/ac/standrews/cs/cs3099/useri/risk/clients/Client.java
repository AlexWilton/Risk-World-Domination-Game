package uk.ac.standrews.cs.cs3099.useri.risk.clients;

import uk.ac.standrews.cs.cs3099.useri.risk.action.Action;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;

/**
 * represents one client.
 * can take multiple forms, eg local, network or AI
 *
 */
public abstract class Client {
	/**
	 * @return the next action this player takes based on current game state
	 */
	public abstract Action getAction();
}
