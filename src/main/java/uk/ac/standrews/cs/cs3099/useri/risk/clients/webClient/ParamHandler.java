package uk.ac.standrews.cs.cs3099.useri.risk.clients.webClient;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.json.simple.parser.JSONParser;
import uk.ac.standrews.cs.cs3099.useri.risk.action.TradeAction;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.WebClient;
import uk.ac.standrews.cs.cs3099.useri.risk.game.ClientApp;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.RiskCard;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.TestGameStateFactory;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.ListenerThread;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.ServerSocketHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class ParamHandler extends DefaultHandler {
    private WebClient webClient;

    public ParamHandler(WebClient webClient){
        super();
        this.webClient = webClient;
    }

    public void handle( String target,
                        Request baseRequest,
                        HttpServletRequest request,
                        HttpServletResponse response ) throws IOException,
            ServletException
    {

        //use test state if not set
//        if(webClient.getState() == null) webClient.setState(TestGameStateFactory.getWebClientTestState(webClient));

        Map<String, String[]> params = request.getParameterMap();
        if (params.size() > 0)
        {
            response.setContentType("text/plain");
            String responseString = "No Operation Specified";
            String[] opArray = params.get("operation");
            String operation = (opArray != null && opArray[0] != null) ?(opArray[0]) : "";
            if(operation != null && !operation.equals("")){
                switch (operation){
                    case "host_game":
                        responseString = createHost(params);
                        break;
                    case "connect":
                        responseString = connectToHost(params);
                        break;
                    case "move_to_game_play":
                        if(webClient.getState() != null){
                            responseString = "true";
                        }else{
                            responseString = "false";
                        }
                        break;
                    case "get_player_id":
                        responseString = String.valueOf(webClient.getPlayerId());
                        break;
                    case "get_state":
                        responseString = webClient.getState().toJSONString();
                        break;
                    case "is_server_waiting_for_action":
                        Player myself = getWebClientPlayer();
                        if(!webClient.isReady())
                            responseString = String.valueOf(true);
                        else
                            responseString = String.valueOf(false);
                        break;
                    case "perform_action":
                        responseString = performAction(params);
                        break;
                    default:
                        responseString = "Operation Not Found";
                }
            }

            if(!operation.equals("is_server_waiting_for_action"))
                System.out.println("Request for operation: " + operation + " received. (" + params.toString() + ")\nResponse sent: " + responseString + "\n");
            response.getWriter().println(responseString);
            baseRequest.setHandled(true);
        }
    }

    private Player getWebClientPlayer(){
        return webClient.getState().getPlayer(webClient.getPlayerId());
    }

    private String performAction(Map<String, String[]> params){
        String[] actionArray = params.get("action");
        String action = (actionArray!= null) ? (actionArray[0]) : "";
        if(action.equals(""))
            return "Error! Action Type not provided";
        switch (action){
            case "trade_in":
                if(!webClient.isReady()){
                    Player myself = getWebClientPlayer();
                    String[] cardIdsAsStrings = params.get("card");
                    ArrayList<RiskCard> cards = new ArrayList<RiskCard>();
                    for(String cardIdAsString : cardIdsAsStrings){
                        int id = Integer.parseInt(cardIdAsString);
                        cards.add(myself.getRiskCardById(id));
                    }
                    TradeAction ta = new TradeAction(getWebClientPlayer(),cards);
                    if(!ta.validateAgainstState(webClient.getState())){
                        return String.valueOf(false);
                    }
                    webClient.queueAction(ta);
                    return String.valueOf(true);
                }else{
                    return String.valueOf(false);
                }
            default:
                return "Unknown Action";
        }
    }


    private String createHost(Map<String, String[]> params) {
        //Get Port to host on
        String[] portArray = params.get("port");
        int port;
        try{
            port = (portArray != null) ? (Integer.parseInt(portArray[0])) : -1;
        }catch (NumberFormatException e){
            port = -1;
        }
        if(port == -1)
            return "Error! Valid Port Number not provided";

        //Get Number Of Players
        String[] numPlayerArray = params.get("number_of_players");
        int numOfPlayers;
        try{
            numOfPlayers = (numPlayerArray != null) ? (Integer.parseInt(numPlayerArray[0])) : -1;
            if(numOfPlayers < ServerSocketHandler.MIN_PLAYER_COUNT || numOfPlayers > ServerSocketHandler.MAX_PLAYER_COUNT)
                numOfPlayers =-1;
        }catch (NumberFormatException e){
            numOfPlayers = -1;
        }
        if(port == -1)
            return "Error! Valid Number of Players not provided";


        //Get Number Of Players
        String[] is_host_playingArray = params.get("is_host_playing");
        boolean is_host_playing;
            if(is_host_playingArray.length == 0)
                return "Error! Valid is Host Playing boolean not provided";
            is_host_playing = Boolean.parseBoolean(numPlayerArray[0]);


        ServerSocketHandler host = new ServerSocketHandler(port, numOfPlayers, webClient, is_host_playing);
        Thread t = new Thread(host);
        t.start();
        
        return "true";
    }




    private String connectToHost(Map<String, String[]> params){
        //Get Host Address
        String[] addressArray = params.get("address");
        String address = (addressArray!= null) ? (addressArray[0]) : "";
        if(address.equals(""))
            return "Error! IP Address not provided";

        //Get Host Port
        String[] portArray = params.get("port");
        int port;
        try{
            port = (portArray != null) ? (Integer.parseInt(portArray[0])) : -1;
        }catch (NumberFormatException e){
            port = -1;
        }
        if(port == -1)
            return "Error! Valid Port Number not provided";

        //Attempt Connection
        if(ClientApp.run(address, port, webClient) != ClientApp.SUCCESS)
            return "Connect to " + address + ":" + port + " failed!";

        return "true";
    }
}

