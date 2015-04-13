package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

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

    public DeployCommand(ArrayList<DeployTuple> lines, int player) {
        super(COMMAND_STRING);
        JSONArray payload = new JSONArray();
        for (DeployTuple t : lines){
            JSONArray line = new JSONArray();
            line.add(t.territory);
            line.add(t.armies);
            payload.add(line);
        }
        this.put("payload",payload);
        this.put("player_id", player);
        this.put("ack_id", ack_id++);

    }
}
