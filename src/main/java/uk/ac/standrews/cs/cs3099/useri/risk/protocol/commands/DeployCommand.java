package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONObject;

/**
 * Created by po26 on 05/03/15.
 */
public class DeployCommand extends Command{


    public static final String COMMAND_STRING = "deploy";

    public DeployCommand(String command) {
        super(command);
    }

    public DeployCommand(JSONObject object){
        super(object);
    }
}
