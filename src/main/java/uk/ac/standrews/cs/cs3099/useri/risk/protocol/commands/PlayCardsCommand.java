package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;


import org.json.simple.JSONObject;

/**
 * Created by po26 on 05/03/15.
 */
public class PlayCardsCommand extends Command {

    public static final String COMMAND_STRING = "play_cards";

    public PlayCardsCommand(String command) {
        super(command);
    }

    public PlayCardsCommand(JSONObject object){
        super(object);
    }
}
