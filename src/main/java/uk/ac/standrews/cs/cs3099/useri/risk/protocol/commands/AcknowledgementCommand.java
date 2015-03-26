package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONObject;

public class AcknowledgementCommand extends Command {

    public static final String COMMAND_STRING = "acknowledgement";

    public AcknowledgementCommand(int id, int response_code, JSONObject data) {
        super(COMMAND_STRING);
        JSONObject payload = new JSONObject();
        payload.put("ack_id", id);
        payload.put("response", response_code);
        payload.put("data", data);
        this.put("payload", payload);
    }

    public AcknowledgementCommand(JSONObject object){
        super(object);
    }
}
