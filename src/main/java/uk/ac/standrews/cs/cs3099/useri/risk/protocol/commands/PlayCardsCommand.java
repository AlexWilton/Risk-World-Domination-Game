package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

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

    public PlayCardsCommand(ArrayList<ArrayList<Integer>> card_triplets, int armies, int player) {
        super(COMMAND_STRING);
        JSONObject payload = new JSONObject();

        JSONArray cards = new JSONArray();
        payload.put("cards",cards);
        payload.put("armies", armies);

        for (ArrayList<Integer> card_triple : card_triplets){
            JSONArray card_triple_json = new JSONArray();
            card_triple_json.addAll(card_triple);
            cards.add(card_triple_json);
        }
        this.put("payload",payload);
        this.put("player_id", player);
        this.put("ack_id", ack_id++);

    }

    public PlayCardsCommand(int player){
        super(COMMAND_STRING);
        this.put("payload",null);
        this.put("player_id", player);
        this.put("ack_id", ack_id++);
    }

}
