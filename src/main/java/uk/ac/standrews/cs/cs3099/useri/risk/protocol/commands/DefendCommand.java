package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONObject;

/**
 * Created by po26 on 05/03/15.
 */
public class DefendCommand extends Command {

    public static final String COMMAND_STRING = "defend";

    public DefendCommand(String command) {
        super(command);
    }

    public DefendCommand(JSONObject object){
        super(object);
    }

    public DefendCommand(int armies, int player) {
        super(COMMAND_STRING);
        this.put("payload",armies);
        this.put("player_id", player);
        this.put("ack_id", getNextAck());

    }
}
