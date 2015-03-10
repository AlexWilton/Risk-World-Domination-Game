package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;


import org.json.simple.JSONArray;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;

public class PlayersJoined extends Command {

    public PlayersJoined(Player[] list) {
        super("players_joined");
        JSONArray payload = new JSONArray();
        for (Player p:list){
            JSONArray arr = new JSONArray();
            if (p != null) {
                arr.add(p.getID());
                arr.add(p.getName());
                payload.add(arr);
            }
        }
        this.put("payload", payload);
    }
}
