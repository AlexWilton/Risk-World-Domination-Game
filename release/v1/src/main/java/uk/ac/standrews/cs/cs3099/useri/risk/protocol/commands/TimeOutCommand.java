package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;


/**
 * Created by bs44 on 11/04/15.
 */
public class TimeOutCommand extends Command {
    public TimeOutCommand(int player_id) {
        super("timeout");
        put("payload", player_id);
        put("player_id", null);
        put("ack_id", getNextAck());
    }
}
