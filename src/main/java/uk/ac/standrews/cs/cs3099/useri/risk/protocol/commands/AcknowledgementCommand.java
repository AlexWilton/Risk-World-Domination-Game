package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class AcknowledgementCommand extends Command {

    public AcknowledgementCommand(int id, int response_code, JSONObject data) {
        super("acknowledgement");
        JSONObject payload = new JSONObject();
        payload.put("ack_id", id);
        payload.put("response", response_code);
        payload.put("data", data);
        this.put("payload", payload);
    }


    public static Command parse(String commandJSON) {
        try{
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(commandJSON);
            if (!obj.get("command").equals("acknowledgement")) {
                return null;
            }
            JSONObject payload = (JSONObject) obj.get("payload");
            int ack_id = Integer.parseInt((String) payload.get("ack_id"));
            int response_code = Integer.parseInt((String) payload.get("response"));
            JSONObject data = (JSONObject) payload.get("data");

            return new AcknowledgementCommand(ack_id, response_code, data);

        } catch(ParseException e){
            System.out.println(commandJSON);
            return null;
        }
    }
}
