package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONObject;

/**
 * Created by bs44 on 19/02/15.
 */
public class Acknowledgement extends Command {

    public Acknowledgement(int id, int response_code, JSONObject data) {
        super("acknowledgement");
        JSONObject payload = new JSONObject();
        payload.put("ack_id", id);
        payload.put("response", response_code);
        payload.put("data", data);
        this.put("paylaod", payload);
    }
}
