package uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


public class Command extends JSONObject {

    static int ack_id = 0;

    public Command(String command){
        this.put("command", command);
    }

    public Command(JSONObject object){
        super(object);
    }

    public static int getLastAckID() {
        return ack_id - 1;
    }

    public JSONObject getPayload(){
        return (JSONObject)this.get("payload");
    }

    public JSONArray getPayloadAsArray(){
        return (JSONArray)this.get("payload");
    }

    public int getPayloadAsInt(){
        return Integer.parseInt(this.get("payload").toString());
    }

    public int getPlayer(){
        Object playerObj = this.get("player_id");
        if(playerObj == null) return -1; //for non-playing host

        return Integer.parseInt(playerObj.toString());
    }

    public String getAsEvelope(String signature){
        String message = this.toJSONString();
        JSONObject ret = new JSONObject();
        ret.put("message", message);
        ret.put("signature", signature);

        return ret.toJSONString();

    }



    public static Command parseCommand(String envelopeString){
        if (envelopeString==null){
            //TODO throw exception
            System.out.println("received string empty");
            return null;
        }

        //extract the actual command
        JSONObject envelopeJSON = (JSONObject) JSONValue.parse(envelopeString);

        //check if its the envelope or the message
        JSONObject commandJSON;

        String unescapedCommandString = "";
        if (envelopeJSON.containsKey("message")){
            //TODO maybe verify?

            unescapedCommandString = unescape(envelopeJSON.get("message").toString());
            commandJSON = (JSONObject) JSONValue.parse(unescapedCommandString);
        }

        else
        {
            unescapedCommandString = envelopeString;
            commandJSON = envelopeJSON;
        }


        String command = commandJSON.get("command").toString();

        Command ret;

        switch (command) {
            case DeployCommand.COMMAND_STRING:
                ret = new DeployCommand(commandJSON);
                break;
            case AttackCommand.COMMAND_STRING:
                ret = new AttackCommand(commandJSON);
                break;
            case PlayCardsCommand.COMMAND_STRING:
                ret = new PlayCardsCommand(commandJSON);
                break;
            case DrawCardCommand.COMMAND_STRING:
                ret = new DrawCardCommand(commandJSON);
                break;
            case DefendCommand.COMMAND_STRING:
                ret = new DefendCommand(commandJSON);
                break;
            case AttackCaptureCommand.COMMAND_STRING:
                ret = new AttackCaptureCommand(commandJSON);
                break;
            case FortifyCommand.COMMAND_STRING:
                ret = new FortifyCommand(commandJSON);
                break;
            case JoinGameCommand.COMMAND_STRING:
                ret = new JoinGameCommand(commandJSON);
                break;
            case AcceptJoinGameCommand.COMMAND_STRING:
                ret = new AcceptJoinGameCommand(commandJSON);
                break;
            case RejectJoinGameCommand.COMMAND_STRING:
                ret = new RejectJoinGameCommand(commandJSON);
                break;
            case AcknowledgementCommand.COMMAND_STRING:
                ret = AcknowledgementCommand.parse(unescapedCommandString);
                break;
            case InitialiseGameCommand.COMMAND_STRING:
                ret = new InitialiseGameCommand(commandJSON);
                break;
            case PingCommand.COMMAND_STRING:
                ret = PingCommand.parse(unescapedCommandString);
                break;
            case PlayersJoinedCommand.COMMAND_STRING:
                ret = new PlayersJoinedCommand(commandJSON);
                break;
            case ReadyCommand.COMMAND_STRING:
                ret = new ReadyCommand(commandJSON);
                break;
            case RollHashCommand.COMMAND_STRING:
                ret = new RollHashCommand(commandJSON);
                break;
            case RollNumberCommand.COMMAND_STRING:
                ret = new RollNumberCommand(commandJSON);
                break;
            case SetupCommand.COMMAND_STRING:
                ret = new SetupCommand(commandJSON);
                break;
            default:
                System.out.println("Cant parse " + command);
                ret = null;
        }

        return ret;
    }

    private static String unescape(String escapedString){
        return escapedString.replace("\\","");
    }

    private static String escapeString(String unescapedString) {
        return unescapedString.replace("\n","\\n").replace("\"", "\"");
    }

    @Override
    public String toJSONString(){
        JSONObject envelope = new JSONObject();
        envelope.put("message",escapeString(super.toJSONString()));
        envelope.put("signature",""); //TODO
        return envelope.toJSONString();
    }

    public boolean requiresTimeout() {return true;}

}
