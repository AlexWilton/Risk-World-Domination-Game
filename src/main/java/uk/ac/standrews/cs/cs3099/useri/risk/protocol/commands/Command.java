package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;

import java.text.ParseException;


public class Command extends JSONObject {

    public Command(String command){
        this.put("command", command);
    }

    public Command(JSONObject object){
        super(object);
    }

    public static Command parseCommand(String commandJSON){
        if (commandJSON==null){
            //TODO throw exception
            System.out.println("received string empty");
            return null;
        }
        JSONObject messageObject;
        messageObject = (JSONObject) JSONValue.parse(commandJSON);

        String command = messageObject.get("command").toString();

        Command ret;

        switch (command) {
            case "deploy":
                ret = DeployCommand.parse(commandJSON);
                break;
            case "trade_in_cards":
                ret = TradeInCardsCommand.parse(commandJSON);
                break;
            case "attack":
                ret = AttackCommand.parse(commandJSON);
                break;
            case "play_cards":
                ret = PlayCardsCommand.parse(commandJSON);
                break;
            case "draw_card":
                ret = DrawCardCommand.parse(commandJSON);
                break;
            case "defend":
                ret = DefendCommand.parse(commandJSON);
                break;
            case "attack_capture":
                ret = AttackCaptureCommand.parse(commandJSON);
                break;
            case "fortify":
                ret = FortifyCommand.parse(commandJSON);
                break;
            case "join_game":
                ret = JoinGameCommand.parse(commandJSON);
                break;
            case "accept_join_game":
                ret = AcceptJoinGameCommand.parse(commandJSON);
                break;
            case "reject_join_game":
                ret = RejectJoinGameCommand.parse(commandJSON);
                break;
            case "ping":
                ret = PingCommand.parse(commandJSON);
                break;
            case "acknowledgement":
                ret = AcknowledgementCommand.parse(commandJSON);
                break;
            default:
                ret = null;
        }

        return ret;
    }
}
