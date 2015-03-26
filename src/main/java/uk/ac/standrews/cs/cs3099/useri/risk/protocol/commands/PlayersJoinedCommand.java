package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;

import java.util.ArrayList;

public class PlayersJoinedCommand extends Command {

    public static final String COMMAND_STRING = "players_joined";

    public PlayersJoinedCommand(ArrayList<Player> list) {
        super("players_joined");
        JSONArray payload = new JSONArray();
        for (Player p:list){
            JSONArray arr = new JSONArray();
            arr.add(p.getID());
            arr.add(p.getName());
            payload.add(arr);
        }
        this.put("payload", payload);
    }
    public PlayersJoinedCommand(JSONObject object){
        super(object);
    }
}
