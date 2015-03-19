package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

public class RejectJoinGameCommand extends Command {

    public RejectJoinGameCommand(String payload) {
        super("reject_join_game");
        this.put("payload", payload);
    }

    public static RejectJoinGameCommand parse(String JSONString){
        return null;
    }
}
