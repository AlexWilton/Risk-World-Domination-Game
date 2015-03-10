package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

public class PingCommand extends Command {

    private final int player_id;
    private final int payload;

    public PingCommand(int id, int payload) {
        super("ping");
        this.player_id = id;
        this.payload = payload;
    }


}
