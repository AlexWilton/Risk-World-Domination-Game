package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JoinGameCommand extends Command {

    public static final String COMMAND_STRING = "join_game";

    JSONArray versions;
    JSONArray features;
    String name;

    public JoinGameCommand(JSONArray versions, JSONArray features){
        super("join_game");
        this.versions = versions;
        this.features = features;

    }

    public JoinGameCommand(JSONArray versions, JSONArray features, String name){
        this(versions, features);
        this.name = name;
    }

    public static Command parse(String parseable) {
        try{
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(parseable);
            if (!obj.get("command").equals("join_game")) {
                return null;
            }
            JSONObject payload = (JSONObject) obj.get("payload");
            JSONArray versions = (JSONArray) payload.get("supported_versions");
            JSONArray features = (JSONArray) payload.get("supported_features");
            String name = (String) payload.get("name");
            if (versions == null) {
                return null;
            }

            return new JoinGameCommand(versions, features, name);

        } catch(ParseException e){
            System.out.println(parseable);
            return null;
        }

    }

    public String getName() {
        return name;
    }

    public JoinGameCommand(JSONObject object){
        super(object);
    }
}
