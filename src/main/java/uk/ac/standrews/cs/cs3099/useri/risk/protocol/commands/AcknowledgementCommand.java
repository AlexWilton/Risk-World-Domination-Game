package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class AcknowledgementCommand extends Command {

    public static final String COMMAND_STRING = "acknowledgement";

    private static int ACK_ID;

    public AcknowledgementCommand(int id, int player_id) {
        super(COMMAND_STRING);
        ACK_ID = id;
        this.put("payload", ACK_ID);
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
            Long payload = (Long) obj.get("payload");
            int ack_id = payload==null? -1 : payload.intValue();
            int playerId = Integer.parseInt(obj.get("player_id").toString());
            System.out.println("AckCommand returned");
            return new AcknowledgementCommand(ack_id, playerId);

        } catch(ParseException e){
            System.out.println(commandJSON);
            return null;
        }
    }

    public int getAcknowledgementId() {
    return ACK_ID;
    }
}
