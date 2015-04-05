package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;


import org.json.simple.JSONObject;

public class RollHashCommand extends Command {
    public static final String COMMAND_STRING = "roll_hash";

    public RollHashCommand(String hash, int player_id){
        super(COMMAND_STRING);

        this.put("payload", hash);
        this.put("player_id", player_id);
    }

    public RollHashCommand(JSONObject object){
        super(object);
    }
}
