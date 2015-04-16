package uk.ac.standrews.cs.cs3099.useri.risk.clients.webClient;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.DefaultHandler;
import uk.ac.standrews.cs.cs3099.useri.risk.action.DeployArmyAction;
import uk.ac.standrews.cs.cs3099.useri.risk.action.SetupAction;
import uk.ac.standrews.cs.cs3099.useri.risk.action.TradeAction;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.WebClient;
import uk.ac.standrews.cs.cs3099.useri.risk.main.ClientApp;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.RiskCard;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.ServerSocketHandler;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.DeployCommand;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.DeployTuple;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.PlayCardsCommand;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.SetupCommand;

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

            if(!operation.equals("is_server_waiting_for_action") && !operation.equals("move_to_game_play")  && !operation.equals("get_state"))
                System.out.println("Request for operation: " + operation + " received. (" + params.toString() + ")\nResponse sent: " + responseString + "\n");
            response.getWriter().println(responseString);
            baseRequest.setHandled(true);
        }
    }

    private Player getWebClientPlayer(){
        return webClient.getState().getPlayer(webClient.getPlayerId());
    }

    private String performAction(Map<String, String[]> params){
        Player myself = getWebClientPlayer();
        String[] actionArray = params.get("action");
        String action = (actionArray!= null) ? (actionArray[0]) : "";
        if(action.equals(""))
            return "Error! Action Type not provided";

        if(webClient.isReady()) //if client already has a command it hasn't yet performed, don't allow a new command!
            return String.valueOf(false);

        switch (action){
            case "setup_claim_country":
                String[] countryIdArray = params.get("country_id");
                int country_id;
                try{
                    country_id = (countryIdArray != null) ? (Integer.parseInt(countryIdArray[0])) : -1;
                }catch (NumberFormatException e){
                    country_id = -1;
                }
                if(country_id == -1)
                    return "Error! Valid Country Id not provided";

                //validate country claim attempt
                SetupAction sa = new SetupAction(myself, webClient.getState().getCountryByID(country_id));
                if(!sa.validateAgainstState(webClient.getState())){
                    return String.valueOf(false);
                }

                SetupCommand setupCommand = new SetupCommand(country_id, myself.getID());
                webClient.queueCommand(setupCommand);
                return String.valueOf(true);

            case "trade_in":
                    if((params.get("no_trade") != null && params.get("no_trade").equals("yes")) || params.get("card") == null){
                        webClient.getState().nextStage();
                        return "true";
                    }

                    String[] cardIdsAsStrings = params.get("card");
                    ArrayList<Integer> cardIDs = new ArrayList<Integer>();
                    ArrayList<RiskCard> riskCards = new ArrayList<RiskCard>();
                    for(String cardIdAsString : cardIdsAsStrings){
                        int id = Integer.parseInt(cardIdAsString);
                        riskCards.add(myself.getRiskCardById(id));
                        cardIDs.add(id);
                    }
                    ArrayList<ArrayList<Integer>> cardTriplets = new ArrayList<ArrayList<Integer>>();
                    cardTriplets.add(cardIDs);
                    TradeAction ta = new TradeAction(myself, riskCards);
                    if(!ta.validateAgainstState(webClient.getState())){
                        return String.valueOf(false);
                    }
                    PlayCardsCommand playCardsCommand = new PlayCardsCommand(cardTriplets, ta.calculateArmies(webClient.getState()), myself.getID());
                    webClient.queueCommand(playCardsCommand);
                    return String.valueOf(true);
            case "deploy_armies":
                    ArrayList<DeployTuple> deployTuples = new ArrayList<DeployTuple>();
                    for(Country myCountry : myself.getOccupiedCountries()){
                        int myCountry_id = myCountry.getCountryId();
                        if(params.containsKey(String.valueOf(myCountry_id))){
                            int armiesToDeployToCountry = Integer.valueOf(params.get(String.valueOf(myCountry_id))[0]);
                            deployTuples.add(new DeployTuple(myCountry_id, armiesToDeployToCountry));

                            //check deploy is allowed
                            DeployArmyAction deployArmyAction = new DeployArmyAction(myself, myCountry, armiesToDeployToCountry);
                            if(!deployArmyAction.validateAgainstState(webClient.getState())){
                                return "Error! Deployment of " + armiesToDeployToCountry + " armies to " + myCountry.getCountryName() + " not allowed";
                            }
                        }
                    }

                    //check army deployments add up to Player's unassignedArmies
                    int armiesToDeployCount = 0;
                    for(DeployTuple dt : deployTuples){
                        armiesToDeployCount += dt.armies;
                    }
                    if(armiesToDeployCount != myself.getUnassignedArmies())
                        return "You Must deploy all " + myself.getUnassignedArmies() + " armies!";



                    DeployCommand deployCommand = new DeployCommand(deployTuples, myself.getID());
                    webClient.queueCommand(deployCommand);
                    return String.valueOf(true);
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
        if(numOfPlayers == -1)
            return "Error! Valid Number of Players not provided";


        //Get Number Of Players
        String[] is_host_playingArray = params.get("is_host_playing");
        boolean is_host_playing;
        if(is_host_playingArray.length == 0)
                return "Error! Valid is Host Playing boolean not provided";
            is_host_playing = Boolean.parseBoolean(is_host_playingArray[0]);

        ServerSocketHandler host = new ServerSocketHandler(port, numOfPlayers, webClient, false);
        Thread t = new Thread(host);
        t.start();
        webClient.setHostAndPlayingBooleans(true, is_host_playing);

        if(is_host_playing) {
            if (ClientApp.run("127.0.0.1", port, webClient) != ClientApp.SUCCESS)
                return "Connect to myself on " + port + "!";
        }

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

        webClient.setHostAndPlayingBooleans(false, false);
        return "true";
    }
}

