package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONObject;

public class Acknowledgement extends Command {

    public Acknowledgement(int id, int response_code, JSONObject data) {
        super("acknowledgement");
        JSONObject payload = new JSONObject();
        payload.put("ack_id", id);
        payload.put("response", response_code);
        payload.put("data", data);
        this.put("payload", payload);
    }
}
