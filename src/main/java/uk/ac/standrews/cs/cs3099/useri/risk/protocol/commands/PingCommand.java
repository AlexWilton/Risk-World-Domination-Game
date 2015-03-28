package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PingCommand extends Command {

    public PingCommand(Integer id, Integer payload) {
        super("ping");
        this.put("player_id", id);
        this.put("payload", payload);
    }


    public static Command parse(String commandJSON) {
        try{
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(commandJSON);
            if (!obj.get("command").equals("ping")) {
                return null;
            }
            Long id_str = (Long) obj.get("player_id");
            Integer id = id_str==null? null:id_str.intValue();

            Long payload_str = (Long) obj.get("payload");
            Integer payload = payload_str==null? null: payload_str.intValue();

            System.out.println("PingCommand parsed: id: " + id + ", payload: " + payload);
            return new PingCommand(id, payload);

        } catch(ParseException e){
            System.out.println(commandJSON);
            return null;
        }

    }
}
