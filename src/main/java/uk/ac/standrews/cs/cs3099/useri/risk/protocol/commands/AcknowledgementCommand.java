package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class AcknowledgementCommand extends Command {

    public static final String COMMAND_STRING = "acknowledgement";


    public AcknowledgementCommand(int ack_id, int player_id) {
        super(COMMAND_STRING);
        this.put("payload", ack_id);
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
            int playerId = Integer.parseInt(obj.get("player_id").toString());
            int ackID = Integer.parseInt(obj.get("payload").toString());
            //System.out.println("AckCommand returned");
            return new AcknowledgementCommand(ackID, playerId);

        } catch(ParseException e){
            System.err.println("could not parse Attack: " + commandJSON);
            return null;
        }
    }

    public int getAcknowledgementId() {
        Integer payload = (Integer) get("payload");
        return payload==null? -1 : payload;
    }

    public boolean requiresAcknowledgement() {return false;}
}
