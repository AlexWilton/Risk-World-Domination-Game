package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONObject;

/**
 * Created by po26 on 05/03/15.
 */
public class FortifyCommand extends Command {

    public static final String COMMAND_STRING = "fortify";

    public FortifyCommand(String command) {
        super(command);
    }

    public FortifyCommand(JSONObject object){
        super(object);
    }
}

