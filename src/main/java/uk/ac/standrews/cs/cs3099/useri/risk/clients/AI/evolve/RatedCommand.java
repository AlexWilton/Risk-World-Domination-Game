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
        if (this.rating - ratedCommand.rating < -0.0001)
            return 1;
        else if (this.rating - ratedCommand.rating > 0.0001)
            return -1;
        else return 0;

    }
}
