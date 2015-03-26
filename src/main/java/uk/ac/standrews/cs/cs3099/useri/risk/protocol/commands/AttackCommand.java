package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONObject;

/**
 * Created by po26 on 05/03/15.
 */
public class AttackCommand extends Command {

    public static final String COMMAND_STRING = "attack";

    public AttackCommand(String command) {
        super(command);
    }


    public AttackCommand(JSONObject object){
        super(object);
    }
}
