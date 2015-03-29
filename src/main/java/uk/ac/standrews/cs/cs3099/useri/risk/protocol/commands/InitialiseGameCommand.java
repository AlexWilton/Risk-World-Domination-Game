package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 */
public class InitialiseGameCommand extends Command{

    public static final String COMMAND_STRING = "initialise_game";

    public InitialiseGameCommand(int version, JSONArray features) {
        super("initialise_game");

        JSONObject payload = new JSONObject();
        payload.put("version", version);
        payload.put("supported_features", features);
        put("payload", payload);
    }

    public InitialiseGameCommand(JSONObject object){
        super(object);
    }
}
