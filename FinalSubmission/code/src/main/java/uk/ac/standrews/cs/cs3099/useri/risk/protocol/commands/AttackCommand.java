package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Attack Command
 */
public class AttackCommand extends Command {

    public static final String COMMAND_STRING = "attack";

    public AttackCommand(String command) {
        super(command);
    }


    public AttackCommand(JSONObject object){
        super(object);
    }

    /**
     * Construct an attack command
     * @param origin Id of country attacking
     * @param target Id of country being attacked
     * @param armies Number of Armies (also the number of dice being thrown by attacker)
     * @param player Attacking Player
     */
    public AttackCommand(int origin, int target, int armies,int player) {
        super(COMMAND_STRING);
        JSONArray payload = new JSONArray();
        payload.add(origin);
        payload.add(target);
        payload.add(armies);
        this.put("payload",payload);
        this.put("player_id", player);
        this.put("ack_id", getNextAck());
    }
}
