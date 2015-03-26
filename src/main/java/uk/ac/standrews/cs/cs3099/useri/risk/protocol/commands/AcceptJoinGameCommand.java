package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;



import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class AcceptJoinGameCommand extends Command {

    public static final String COMMAND_STRING = "accept_join_game";

    public AcceptJoinGameCommand(int ack_timeout, int move_timeout, int id) {
        super(COMMAND_STRING);
        JSONObject payload = new JSONObject();
        payload.put("player_id", id);
        payload.put("acknowledgement_timeout", ack_timeout);
        payload.put("move_timeout", move_timeout);
        this.put("payload", payload);
    }

    public AcceptJoinGameCommand(JSONObject object){
        super(object);
    }

}
