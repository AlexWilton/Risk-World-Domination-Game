package uk.ac.standrews.cs.cs3099.useri.risk.clients.webClient;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.DefaultHandler;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.WebClient;
import uk.ac.standrews.cs.cs3099.useri.risk.game.ClientApp;

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
                        String[] ipArray = params.get("ip");
                        String ip = (ipArray != null) ? (ipArray[0]) : "";
                        if(ip.equals("")){
                            responseString = "Error! IP Address not provided";
                            break;
                        }
                        String[] portArray = params.get("port");
                        int port;
                        try{
                            port = (portArray != null) ? (Integer.parseInt(portArray[0])) : -1;
                        }catch (NumberFormatException e){ port = -1;}
                        if(port == -1){
                            responseString = "Error! Valid Port Number not provided";
                            break;
                        }

                        if(ClientApp.run(ip, port, webClient) != ClientApp.SUCCESS){
                            responseString = "Connect to " + ip + ":" + port + " failed!";
                            break;
                        }
                        break;
                    case "get_state":
                        break;
                    default:
                        responseString = "Operation Not Found";
                }
            }

            response.getWriter().println(responseString);
            baseRequest.setHandled(true);
        }
    }
}

