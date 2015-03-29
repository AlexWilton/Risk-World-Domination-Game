package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class AcknowledgementCommand extends Command {

    public static final String COMMAND_STRING = "acknowledgement";

    private static int ACK_ID;

    public AcknowledgementCommand(int id, int response_code, JSONObject data, int player_id) {
        super(COMMAND_STRING);
        JSONObject payload = new JSONObject();
        ACK_ID = id;
        payload.put("ack_id", id);
        payload.put("response", response_code);
        payload.put("data", data);
        this.put("payload", payload);
        this.put("player_id", player_id);
    }

    public AcknowledgementCommand(JSONObject object){
        super(object);
    }


    public static Command parse(String commandJSON) {
        try{
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(commandJSON);
            if (!obj.get("command").equals("acknowledgement")) {
                return null;
            }
            JSONObject payload = (JSONObject) obj.get("payload");
            Long ack_id_str = (Long) payload.get("ack_id");
            int ack_id = ack_id_str==null? -1 : ack_id_str.intValue();
            Long response_code_str = (Long) payload.get("response");
            int response_code = response_code_str==null? -1 : response_code_str.intValue();
            JSONObject data = (JSONObject) payload.get("data");
            int playerId = Integer.parseInt(obj.get("player_id").toString());
            System.out.println("AckCommand returned");
            return new AcknowledgementCommand(ack_id, response_code, data,playerId);

        } catch(ParseException e){
            System.out.println(commandJSON);
            return null;
        }
    }

    public int getAcknowledgementId() {
    return ACK_ID;
    }
}
