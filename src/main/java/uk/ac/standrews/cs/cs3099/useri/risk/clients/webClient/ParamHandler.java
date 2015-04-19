package uk.ac.standrews.cs.cs3099.useri.risk.clients.webClient;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.json.simple.JSONArray;
import uk.ac.standrews.cs.cs3099.useri.risk.action.*;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.WebClient;
import uk.ac.standrews.cs.cs3099.useri.risk.main.AIApp;
import uk.ac.standrews.cs.cs3099.useri.risk.main.AIRunner;
import uk.ac.standrews.cs.cs3099.useri.risk.main.ClientApp;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.RiskCard;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.ServerSocketHandler;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class ParamHandler extends DefaultHandler {
    private WebClient webClient;
    private ServerSocketHandler host;

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
                    case "get_list_of_players_connected_to_host":
                        JSONArray playerNames = new JSONArray();
                        for(String name : host.getConnectedPlayerNames())
                            playerNames.add(name);
                        responseString = playerNames.toJSONString();
                        break;
                    case "get_list_of_available_ai":
                        JSONArray aiNames = new JSONArray();
                        for(String aiName : AIApp.getListOfAvailableAIs()){
                            aiNames.add(aiName);
                        }
                        responseString = aiNames.toJSONString();
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

            if(!operation.equals("is_server_waiting_for_action") && !operation.equals("move_to_game_play")  && !operation.equals("get_state") && !operation.equals("get_list_of_players_connected_to_host"))
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
                    if((params.get("no_trade") != null && params.get("no_trade")[0].equals("yes")) || params.get("card") == null){
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

            case "attack":
                if((params.get("end_attack") != null && params.get("end_attack")[0].equals("yes"))){
                    webClient.getState().nextStage();
                    //check if a risk card needs to be obtained...
                    ObtainRiskCardAction obtainRiskCardAction = new ObtainRiskCardAction(myself);
                    if(obtainRiskCardAction.validateAgainstState(webClient.getState())){
                        DrawCardCommand drawCardCommand = new DrawCardCommand(webClient.getState().peekCard().getCardID(), myself.getID());
                        webClient.queueCommand(drawCardCommand);
                    }else{
                        webClient.getState().nextStage(); //skip get card stage if there is no card to draw.
                    }

                    return "true";
                }

                //Get attacking and defending Country objects
                String[] attackingCountryArray = params.get("attacking_country_id");
                String[] defendingCountryArray = params.get("defending_country_id");
                int attacking_country_id, defending_country_id;
                try{
                    attacking_country_id = (attackingCountryArray != null) ? (Integer.parseInt(attackingCountryArray[0])) : -1;
                    defending_country_id = (defendingCountryArray != null) ? (Integer.parseInt(defendingCountryArray[0])) : -1;
                }catch (NumberFormatException e){
                    return "Error! Valid Country Id not provided";
                }
                Country attackingCountry = webClient.getState().getCountryByID(attacking_country_id);
                Country defendingCountry = webClient.getState().getCountryByID(defending_country_id);
                if(attackingCountry == null || attackingCountry.getOwner() != myself)
                    return "Error! Please attacked from a valid country.";
                if(defendingCountry == null || defendingCountry.getOwner() == myself)
                    return "Error! Please chose a valid country to attack";

                //Get number of armies of attack
                String[] numOfArmiesArray = params.get("num_of_armies");
                int num_of_armies;
                try {
                    num_of_armies = Integer.parseInt(numOfArmiesArray[0]);
                    if(num_of_armies < 1 || num_of_armies > 3 || num_of_armies >= attackingCountry.getTroops()) throw new NumberFormatException();
                }catch (NumberFormatException e){
                    return "Error! Please select a valid number of armies to attack with";
                }

                AttackCommand attackCommand = new AttackCommand(attacking_country_id, defending_country_id, num_of_armies, myself.getID());
                webClient.queueCommand(attackCommand);
                return String.valueOf(true);

            case "attack_capture":
                //Get attacking and defending Country objects
                attackingCountryArray = params.get("attacking_country_id");
                defendingCountryArray = params.get("defending_country_id");
                try{
                    attacking_country_id = (attackingCountryArray != null) ? (Integer.parseInt(attackingCountryArray[0])) : -1;
                    defending_country_id = (defendingCountryArray != null) ? (Integer.parseInt(defendingCountryArray[0])) : -1;
                }catch (NumberFormatException e){
                    return "Error! Valid Country Id not provided";
                }
                attackingCountry = webClient.getState().getCountryByID(attacking_country_id);
                defendingCountry = webClient.getState().getCountryByID(defending_country_id);
                if(attackingCountry == null || attackingCountry.getOwner() != myself)
                    return "Error! Please attacked from a valid country.";
                if(defendingCountry == null || defendingCountry.getOwner() != myself) //before capture, country is set to new owner when attack action is performed
                    return "Error! Please chose a valid country to attack";

                //Get number of armies of attack
                numOfArmiesArray = params.get("num_of_armies");
                try {
                    num_of_armies = Integer.parseInt(numOfArmiesArray[0]);
                    if(num_of_armies < 1) throw new NumberFormatException();
                }catch (NumberFormatException e){
                    return "Error! Please select a valid number of armies to attack with";
                }

                AttackCaptureAction attackCaptureAction = new AttackCaptureAction(myself, attacking_country_id, defending_country_id, num_of_armies);
                if(!attackCaptureAction.validateAgainstState(webClient.getState())){
                    return "Error! This movement is not allowed.";
                }

                AttackCaptureCommand attackCaptureCommand = new AttackCaptureCommand(attacking_country_id, defending_country_id, num_of_armies, myself.getID());
                webClient.queueCommand(attackCaptureCommand);
                return String.valueOf("true");

            case "fortify": //skip_fortity
                if((params.get("skip_fortity") != null && params.get("skip_fortity")[0].equals("yes"))){
                    FortifyCommand fortifyCommand = new FortifyCommand(myself.getID());
                    webClient.queueCommand(fortifyCommand);
                    return String.valueOf("true");
                }


                //Get attacking and defending Country objects
                String[] originCountryArray = params.get("origin_country_id");
                String[] destinationCountryArray = params.get("destination_country_id");
                int origin_country_id, destination_country_id;
                try{
                    origin_country_id = (originCountryArray != null) ? (Integer.parseInt(originCountryArray[0])) : -1;
                    destination_country_id = (destinationCountryArray != null) ? (Integer.parseInt(destinationCountryArray[0])) : -1;
                }catch (NumberFormatException e){
                    return "Error! Valid Country Id not provided";
                }
                Country originCountry = webClient.getState().getCountryByID(origin_country_id);
                Country destinationCountry = webClient.getState().getCountryByID(destination_country_id);
                if(originCountry == null || originCountry.getOwner() != myself)
                    return "Error! Please fortify from a valid country.";
                if(destinationCountry == null || destinationCountry.getOwner() != myself) //before capture, country is set to new owner when attack action is performed
                    return "Error! Please chose a valid country to fortify.";

                //Get number of armies for fortification
                numOfArmiesArray = params.get("num_of_armies");
                try {
                    num_of_armies = Integer.parseInt(numOfArmiesArray[0]);
                    if(num_of_armies < 1) throw new NumberFormatException();
                }catch (NumberFormatException e){
                    return "Error! Please select a valid number of armies to fortify with";
                }

                FortifyAction fortifyAction = new FortifyAction(myself, originCountry, destinationCountry, num_of_armies);
                if(!fortifyAction.validateAgainstState(webClient.getState())){
                    return "Error! This fortify movement is not allowed.";
                }

                FortifyCommand fortifyCommand = new FortifyCommand(origin_country_id, destination_country_id, num_of_armies, myself.getID());
                webClient.queueCommand(fortifyCommand);
                return String.valueOf("true");

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

        host = new ServerSocketHandler(port, numOfPlayers, webClient, false);
        Thread t = new Thread(host);
        t.start();
        webClient.setHostAndPlayingBooleans(true, is_host_playing);

        //Get Player's Name
        String[] nameArray = params.get("player_name");
        String playerName = "";
        if(nameArray != null && nameArray.length > 0)
            playerName = nameArray[0];


        if(is_host_playing) {
            if (ClientApp.run("127.0.0.1", port, webClient, playerName) != ClientApp.SUCCESS)
                return "Connect to myself on " + port + "!";
        }

        //check to see if we need to add ai players
        String[] aiPlayerArray = params.get("ai_player");
        if(aiPlayerArray != null) {
            for (int i = 0; i < aiPlayerArray.length; i++) {
                String[] aiPlayerData = aiPlayerArray[i].split(",");
                String aiType = aiPlayerData[0];
                String aiName = aiPlayerData[1];
                AIRunner aiRunner = new AIRunner(AIApp.createAiClient(aiType), aiName, "127.0.0.1", port);
                (new Thread(aiRunner)).start();
            }
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

        //Get Player's Name
        String[] nameArray = params.get("player_name");
        String playerName = "";
        if(nameArray != null && nameArray.length > 0)
            playerName = nameArray[0];

        //Attempt Connection
        if(ClientApp.run(address, port, webClient, playerName) != ClientApp.SUCCESS)
            return "Connect to " + address + ":" + port + " failed!";

        webClient.setHostAndPlayingBooleans(false, false);
        return "true";
    }
}

