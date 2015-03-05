package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONObject;

public class AcceptJoinGame extends Command {

    public AcceptJoinGame(int ack_timeout, int move_timeout, int id) {
        super("accept_join_game");
        JSONObject payload = new JSONObject();
        payload.put("player_id", id);
        payload.put("acknowledgement_timeout", ack_timeout);
        payload.put("move_timeout", move_timeout);
        this.put("payload", payload);
    }


    public static Command parse(String JSONString) {
        return null;
    } //TODO
}
