package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Created by po26 on 05/03/15.
 */
public class DeployCommand extends Command{


    public static final String COMMAND_STRING = "deploy";

    public DeployCommand(String command) {
        super(command);
    }

    public DeployCommand(JSONObject object){
        super(object);
    }

    public DeployCommand(int territory, int armies, int player) {
        super(COMMAND_STRING);
        JSONArray payload = new JSONArray();
        JSONArray pair = new JSONArray();
        pair.add(0, territory);
        pair.add(1, armies);
        payload.add(0,pair);
        this.put("payload",payload);
        this.put("player_id", player);
        this.put("ack_id", "1");

    }
}
