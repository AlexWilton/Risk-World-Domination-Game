package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONObject;

/**
 * Created by po26 on 05/03/15.
 */
public class SetupCommand extends Command {

    public static final String COMMAND_STRING = "setup";

    public SetupCommand(String command) {
        super(command);
    }

    public SetupCommand(JSONObject object){
        super(object);
    }

    public SetupCommand(int target, int player) {
        super(COMMAND_STRING);
        this.put("payload",target);
        this.put("player_id", player);
        this.put("ack_id", "1");

    }
}

