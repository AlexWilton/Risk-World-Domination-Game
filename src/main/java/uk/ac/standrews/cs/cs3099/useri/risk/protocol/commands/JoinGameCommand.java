package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;

public class JoinGameCommand extends Command {

    public static final String COMMAND_STRING = "join_game";

    JSONArray versions;
    JSONArray features;
    String name;



    public JoinGameCommand(JSONArray versions, JSONArray features, String name){
        super("join_game");
        this.versions = versions;
        this.features = features;
        JSONObject payload = new JSONObject();
        payload.put("supported_versions", versions);
        payload.put("supported_features", features);
        payload.put("name", name);
        this.put("payload", payload);
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
            if (features == null)
                features = new JSONArray();

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
        JSONObject payload = (JSONObject) object.get("payload");
        versions = (JSONArray) payload.get("supported_versions");
        features = (JSONArray) payload.get("supported_features");
        name = (String) payload.get("name");
    }

    public ArrayList<String> getFeatures() {
        ArrayList<String> list = new ArrayList<>();
        for (Object a : features){
            list.add((String) a);
            System.out.println(a);

        }
        return list;
    }

    public int getVersion() {
        int i = 0;
        for (Object a : versions){
            if (a instanceof Long){
                if (((Long) a).intValue() > i){
                    i = (((Long) a).intValue());
                }
            }
        }
        return i;
    }

    public boolean requiresTimeout() {return false;}
}