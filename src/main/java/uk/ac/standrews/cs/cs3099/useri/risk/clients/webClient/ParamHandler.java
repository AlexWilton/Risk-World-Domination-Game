package uk.ac.standrews.cs.cs3099.useri.risk.clients.webClient;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.json.simple.parser.JSONParser;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.WebClient;
import uk.ac.standrews.cs.cs3099.useri.risk.game.ClientApp;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.TestGameStateFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class ParamHandler extends DefaultHandler {
    private WebClient webClient;

    public ParamHandler(WebClient webClient){
        super();
        this.webClient = webClient;
        webClient.setState(TestGameStateFactory.getTestGameState()); //TODO remove testing STATE!!!
    }

    public void handle( String target,
                        Request baseRequest,
                        HttpServletRequest request,
                        HttpServletResponse response ) throws IOException,
            ServletException
    {
        Map<String, String[]> params = request.getParameterMap();
        if (params.size() > 0)
        {
            response.setContentType("text/plain");
            String responseString = "No Operation Specified";
            String[] opArray = params.get("operation");
            String operation = (opArray != null) ?(opArray[0]) : "";
            if(operation != null && !operation.equals("")){
                switch (operation){
                    case "connect":
                        responseString = connectToHost(params);
                        break;
                    case "get_state":
                        responseString = webClient.getState().toJSONString();
                        break;
                    default:
                        responseString = "Operation Not Found";
                }
            }

            response.getWriter().println(responseString);
            baseRequest.setHandled(true);
        }
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

        return "Connection Successful";
    }
}

