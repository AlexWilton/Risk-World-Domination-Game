package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

public class PingCommand extends Command {

    public PingCommand(Integer id, int payload) {
        super("ping");
        this.put("player_id", id);
        this.put("payload", payload);
    }


}
