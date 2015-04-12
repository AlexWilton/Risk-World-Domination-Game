package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Created by po26 on 05/03/15.
 */
public class AttackCommand extends Command {

    public static final String COMMAND_STRING = "attack";

    public AttackCommand(String command) {
        super(command);
    }


    public AttackCommand(JSONObject object){
        super(object);
    }

    public AttackCommand(int origin, int target, int armies,int player) {
        super(COMMAND_STRING);
        JSONArray payload = new JSONArray();
        payload.add(origin);
        payload.add(target);
        payload.add(armies);
        this.put("payload",payload);
        this.put("player_id", player);
        this.put("ack_id", ack_id++);

    }
}
