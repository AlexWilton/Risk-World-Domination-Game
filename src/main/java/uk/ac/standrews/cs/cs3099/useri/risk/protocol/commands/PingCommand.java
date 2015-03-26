package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONObject;

public class PingCommand extends Command {

    public static final String COMMAND_STRING = "ping";

    public PingCommand(Integer id, int payload) {
        super("ping");
        this.put("player_id", id);
        this.put("payload", payload);
    }

    public PingCommand(JSONObject object){
        super(object);
    }

}
