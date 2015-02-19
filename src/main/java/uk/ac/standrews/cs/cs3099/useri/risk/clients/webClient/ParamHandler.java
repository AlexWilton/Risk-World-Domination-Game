package uk.ac.standrews.cs.cs3099.useri.risk.clients.webClient;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.DefaultHandler;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class ParamHandler extends DefaultHandler {
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
            response.getWriter().println(params.toString());
            baseRequest.setHandled(true);
        }
    }
}