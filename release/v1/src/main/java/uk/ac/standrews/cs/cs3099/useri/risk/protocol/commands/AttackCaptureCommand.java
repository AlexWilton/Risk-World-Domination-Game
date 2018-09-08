package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Attack Capture Command
 */
public class AttackCaptureCommand extends Command {

    public static final String COMMAND_STRING = "attack_capture";

    public AttackCaptureCommand(String command) {
        super(command);
    }

    public AttackCaptureCommand(JSONObject object) {
        super(object);
    }

    /**
     * Construct an attack capture command.
     * Number of armies moved in must be at least the number of dice rolled and
     * at least one army must be least in the origin country.
     *
     * @param origin Id of country attacking
     * @param target Id of country being attacked
     * @param armies Number of Armies to move into the capture Territory.
     * @param player Attacking Player
     */
    public AttackCaptureCommand(int origin, int target, int armies, int player) {
        super(COMMAND_STRING);
        JSONArray payload = new JSONArray();
        payload.add(0,origin);
        payload.add(1,target);
        payload.add(2,armies);
        this.put("payload", payload);
        this.put("player_id", player);
        this.put("ack_id", getNextAck());
    }


    public int getOrigin() {
        JSONArray payload = (JSONArray) this.get("payload");
        int origin = Integer.parseInt(payload.get(0).toString());
        return origin;
    }

    public int getDestination() {
        JSONArray payload = (JSONArray) this.get("payload");
        int destination = Integer.parseInt(payload.get(1).toString());
        return destination;
    }

    public int getArmies() {
        JSONArray payload = (JSONArray) this.get("payload");
        int armies = Integer.parseInt(payload.get(2).toString());
        return armies;
    }

    public int getPlayer() {
        int player_id = Integer.parseInt(get("player_id").toString());
        return player_id;
    }
}

