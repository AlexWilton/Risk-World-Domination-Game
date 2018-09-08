package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;


import org.json.simple.JSONObject;

public class RollNumberCommand extends Command {
    public static final String COMMAND_STRING = "roll_number";

    public RollNumberCommand(String number, int player_id){
        super(COMMAND_STRING);

        this.put("payload", number);
        this.put("player_id", player_id);
    }

    public RollNumberCommand(JSONObject object){
        super(object);
    }

    @Override
    public boolean requiresAcknowledgement() {
        return false;
    }
}
