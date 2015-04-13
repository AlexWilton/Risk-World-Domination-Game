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
        JSONArray triple = new JSONArray();
        triple.add(origin);
        triple.add(target);
        triple.add(armies);
        payload.add(triple);
        this.put("payload",payload);
        this.put("player_id", player);
        this.put("ack_id", ack_id++);

    }

    public FortifyCommand(int player) {
        super(COMMAND_STRING);


        this.put("payload",null);
        this.put("player_id", player);
        this.put("ack_id", "1");

    }
}

