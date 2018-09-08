package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Created by po26 on 05/03/15.
 */
public class FortifyCommand extends Command {

    public static final String COMMAND_STRING = "fortify";

    public FortifyCommand(String command) {
        super(command);
    }

    public FortifyCommand(JSONObject object){
        super(object);
    }

    public FortifyCommand(int origin, int target, int armies,int player) {
        super(COMMAND_STRING);
        JSONArray payload = new JSONArray();

        payload.add(origin);
        payload.add(target);
        payload.add(armies);
        this.put("payload",payload);
        this.put("player_id", player);
        this.put("ack_id", getNextAck());

    }

    public FortifyCommand(int player) {
        super(COMMAND_STRING);

        this.put("payload",null);
        this.put("player_id", player);
        this.put("ack_id", getNextAck());
    }
}

