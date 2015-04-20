package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve;

import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.Command;

/**
 * Created by po26 on 20/04/15.
 */
public class RatedCommand implements Comparable<RatedCommand>{

    public final double rating;
    public final Command command;


    public RatedCommand(Command c ,double rating ){
        this.rating = rating;
        this.command = c;
    }

    @Override
    public int compareTo(RatedCommand ratedCommand) {
        return 0;
    }
}
