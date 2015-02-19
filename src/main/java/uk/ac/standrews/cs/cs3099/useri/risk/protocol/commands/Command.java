package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONObject;

/**
 * Created by bs44 on 19/02/15.
 */
public class Command extends JSONObject {

    public Command(String command){
        this.put("command", command);
    }
}
