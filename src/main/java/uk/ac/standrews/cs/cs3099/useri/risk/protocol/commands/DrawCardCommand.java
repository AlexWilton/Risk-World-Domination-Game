package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONObject;

/**
 * Created by po26 on 05/03/15.
 */
public class DrawCardCommand extends Command {

    public static final String COMMAND_STRING = "draw_card";

    public DrawCardCommand(String command) {
        super(command);
    }

    public DrawCardCommand(JSONObject object){
        super(object);
    }
}
