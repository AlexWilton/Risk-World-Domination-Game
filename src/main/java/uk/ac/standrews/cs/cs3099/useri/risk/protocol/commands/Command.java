package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONObject;

public class Command extends JSONObject {

    public Command(String command){
        this.put("command", command);
    }
}
