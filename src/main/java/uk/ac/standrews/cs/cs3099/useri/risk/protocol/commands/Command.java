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
        System.out.println(commandJSON);
        JSONObject messageObject;
        messageObject = (JSONObject) JSONValue.parse(commandJSON);

        String command = messageObject.get("command").toString();

        Command ret;

        switch (command) {
            case DeployCommand.COMMAND_STRING:
                ret = new DeployCommand(messageObject);
                break;
            case AttackCommand.COMMAND_STRING:
                ret = new AttackCommand(messageObject);
                break;
            case PlayCardsCommand.COMMAND_STRING:
                ret = new PlayCardsCommand(messageObject);
                break;
            case DrawCardCommand.COMMAND_STRING:
                ret = new DrawCardCommand(messageObject);
                break;
            case DefendCommand.COMMAND_STRING:
                ret = new DefendCommand(messageObject);
                break;
            case AttackCaptureCommand.COMMAND_STRING:
                ret = new AttackCaptureCommand(messageObject);
                break;
            case FortifyCommand.COMMAND_STRING:
                ret = new FortifyCommand(messageObject);
                break;
            case JoinGameCommand.COMMAND_STRING:
                ret = new JoinGameCommand(messageObject);
                break;
            case AcceptJoinGameCommand.COMMAND_STRING:
                ret = new AcceptJoinGameCommand(messageObject);
                break;
            case RejectJoinGameCommand.COMMAND_STRING:
                ret = new RejectJoinGameCommand(messageObject);
                break;
            case AcknowledgementCommand.COMMAND_STRING:
                ret = AcknowledgementCommand.parse(commandJSON);
                break;
            case InitialiseGameCommand.COMMAND_STRING:
                ret = new InitialiseGameCommand(messageObject);
                break;
            case PingCommand.COMMAND_STRING:
                ret = PingCommand.parse(commandJSON);
                break;
            case PlayersJoinedCommand.COMMAND_STRING:
                ret = new PlayersJoinedCommand(messageObject);
                break;
            case ReadyCommand.COMMAND_STRING:
                ret = new ReadyCommand(messageObject);
                break;
            default:
                ret = null;
        }

        return ret;
    }
}
