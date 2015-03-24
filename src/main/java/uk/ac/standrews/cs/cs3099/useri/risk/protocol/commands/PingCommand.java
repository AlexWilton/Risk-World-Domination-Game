package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PingCommand extends Command {

    public PingCommand(Integer id, int payload) {
        super("ping");
        this.put("player_id", id);
        this.put("payload", payload);
    }


    public static Command parse(String commandJSON) {
        try{
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(commandJSON);
            if (!obj.get("command").equals("join_game")) {
                return null;
            }
            Integer id = (Integer) obj.get("player_id");
            int payload = Integer.parseInt((String) obj.get("payload"));

            return new PingCommand(id, payload);

        } catch(ParseException e){
            System.out.println(commandJSON);
            return null;
        }

    }
}
