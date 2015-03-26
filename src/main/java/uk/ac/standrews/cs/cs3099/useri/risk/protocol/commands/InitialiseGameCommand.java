package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Created by bentlor on 23/03/15.
 */
public class InitialiseGameCommand extends Command{

    public static final String COMMAND_STRING = "initialise_game";

    public InitialiseGameCommand(int version, JSONArray features) {
        super("initialise_game");

        JSONObject payload = new JSONObject();
        payload.put("version", version);
        payload.put("supported_features", features);
        this.put("payload", payload);
    }

    public InitialiseGameCommand(JSONObject object){
        super(object);
    }
}
