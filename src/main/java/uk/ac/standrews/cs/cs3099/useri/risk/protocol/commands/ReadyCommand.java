package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

/**
 * Created by bentlor on 19/03/15.
 */
public class ReadyCommand extends Command {
    public ReadyCommand(Integer id, int ack_id) {
        super("ready");
        this.put("payload", null);
        this.put("player_id", id);
        this.put("ack_id", ack_id);
    }
}
