package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

public class RejectJoinGame extends Command {

    public RejectJoinGame(String payload) {
        super("reject_join_game");
        this.put("payload", payload);
    }

    public static RejectJoinGame parse(String JSONString){
        return null;
    }
}
