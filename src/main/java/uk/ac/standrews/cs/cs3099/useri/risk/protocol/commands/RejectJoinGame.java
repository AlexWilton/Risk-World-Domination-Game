package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONObject;

/**
 * Created by bs44 on 19/02/15.
 */
public class RejectJoinGame extends Command {

    public RejectJoinGame(String payload) {
        super("reject_join_game");
        this.put("payload", payload);
    }
}
