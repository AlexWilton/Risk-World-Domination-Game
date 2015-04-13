package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONObject;

/**
 * Created by bentlor on 19/03/15.
 */
public class ReadyCommand extends Command {


    public static final String COMMAND_STRING = "ready";
    
    public ReadyCommand(Integer id, int ack_id) {
        super("ready");
        this.put("payload", null);
        this.put("player_id", id);
        this.put("ack_id", ack_id++);
    }
    public ReadyCommand(JSONObject object){
        super(object);
    }
}
