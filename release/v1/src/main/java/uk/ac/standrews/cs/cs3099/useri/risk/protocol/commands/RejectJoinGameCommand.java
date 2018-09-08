package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONObject;

public class RejectJoinGameCommand extends Command {

    public static final String COMMAND_STRING = "reject_join_game";

    public RejectJoinGameCommand(String payload) {
        super("reject_join_game");
        this.put("payload", payload);
    }

    public RejectJoinGameCommand(JSONObject object){
        super(object);
    }
}
