package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;


import org.json.simple.JSONArray;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;

import java.util.ArrayList;

public class PlayersJoinedCommand extends Command {

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
}
