package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;


import org.json.simple.JSONObject;

public class RollCommand extends Command {
    public static final String COMMAND_STRING = "roll";

    public RollCommand(int diceCount, int diceFaces, int player_id){
        super(COMMAND_STRING);
        JSONObject payload = new JSONObject();
        payload.put("dice_count", diceCount);
        payload.put("dice_faces", diceFaces);
        this.put("payload", payload);
        this.put("player_id", player_id);
    }

    public RollCommand(JSONObject object){
        super(object);
    }

}
