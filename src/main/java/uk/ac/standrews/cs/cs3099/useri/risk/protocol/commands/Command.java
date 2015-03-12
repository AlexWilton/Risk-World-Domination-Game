package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


public class Command extends JSONObject {

    public Command(String command){
        this.put("command", command);
    }

    public Command(JSONObject object){
        super(object);
    }

    public static Command parseCommand(String commandJSON){

        JSONObject messageObject;
        messageObject = (JSONObject) JSONValue.parse(commandJSON);

        String command = messageObject.get("command").toString();

        Command ret;

        switch (command) {
            case "deploy":
                ret = Deploy.parse(commandJSON);
                break;
            case "trade_in_cards":
                ret = TradeInCards.parse(commandJSON);
                break;
            case "attack":
                ret = Attack.parse(commandJSON);
                break;
            case "play_cards":
                ret = PlayCards.parse(commandJSON);
                break;
            case "draw_card":
                ret = DrawCard.parse(commandJSON);
                break;
            case "defend":
                ret = Defend.parse(commandJSON);
                break;
            case "attack_capture":
                ret = AttackCapture.parse(commandJSON);
                break;
            case "fortify":
                ret = Fortify.parse(commandJSON);
                break;
            case "join_game":
                ret = JoinGame.parse(commandJSON);
                break;
            case "accept_join_game":
                ret = AcceptJoinGame.parse(commandJSON);
                break;
            case "reject_join_game":
                ret = RejectJoinGame.parse(commandJSON);
                break;
            default:
                ret = null;
        }

        return ret;
    }
}
