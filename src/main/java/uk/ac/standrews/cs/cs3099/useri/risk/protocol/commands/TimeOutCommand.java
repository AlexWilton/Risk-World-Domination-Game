package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;


/**
 * Created by bs44 on 11/04/15.
 */
public class TimeOutCommand extends Command {
    public TimeOutCommand(int player_id, Integer ID) {
        super("timeout");
        put("payload", player_id);
        put("player_id", ID);
        put("ack_id", ack_id++);
    }
}
