package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Attack Capture Command
 */
class AttackCaptureCommand extends Command {

    public static final String COMMAND_STRING = "attack_capture";

    public AttackCaptureCommand(String command) {
        super(command);
    }

    public AttackCaptureCommand(JSONObject object){
        super(object);
    }

    /**
     * Construct an attack capture command.
     * Number of armies moved in must be at least the number of dice rolled and
     * at least one army must be least in the origin country.
     * @param origin Id of country attacking
     * @param target Id of country being attacked
     * @param armies Number of Armies to move into the capture Territory.
     * @param player Attacking Player
     */
    public AttackCaptureCommand(int origin, int target, int armies,int player) {
        super(COMMAND_STRING);
        JSONArray payload = new JSONArray();
        payload.add(origin);
        payload.add(target);
        payload.add(armies);
        this.put("payload",payload);
        this.put("player_id", player);
        this.put("ack_id", ack_id++);
    }

}

