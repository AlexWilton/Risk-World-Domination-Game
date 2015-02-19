package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Created by bs44 on 19/02/15.
 */
public class JoinGame extends Command {

    JSONArray versions;
    JSONArray features;

    public JoinGame(JSONArray versions, JSONArray features){
        super("join_game");
    }

    public static JoinGame parse(String parseable){
        try{
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(parseable);
            if (!obj.get("command").equals("join_game"))
                return null;
            JSONObject payload = (JSONObject) obj.get("payload");
            JSONArray versions = (JSONArray) payload.get("supported_versions");
            JSONArray features = (JSONArray) payload.get("supported_features");
            if (versions == null)
                return null;

            return new JoinGame(versions, features);

        } catch(ParseException e){
            return null;
        }

    }
}
