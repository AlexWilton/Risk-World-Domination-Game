package uk.ac.standrews.cs.cs3099.useri.risk.clients;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.AI;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;

/**
 * Created by ryo_yanagida on 17/04/2015.
 */
public abstract class AIClient extends Client {

    AI ai;

    public AIClient(State gamestate, AI ai) {
        super(gamestate);
        this.ai = ai;
    }



    public abstract boolean isLocal();
}
