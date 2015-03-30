package uk.ac.standrews.cs.cs3099.useri.risk.clients.webClient;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.webapp.WebAppContext;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.WebClient;

import java.net.BindException;

public class JettyServer implements Runnable {
    private static final String WEB_CLIENT_FILEPATH = "web_client";
    private Server server = null;

    public JettyServer(WebClient webClient){
        server = new Server(0); //find random open port on runtime

        // create the handlers
        Handler dataRequestHandler = new ParamHandler(webClient);

        WebAppContext fileHandler = new WebAppContext();
        fileHandler.setContextPath("/");
        fileHandler.setResourceBase(WEB_CLIENT_FILEPATH);

        // create the handler collections
        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[] { dataRequestHandler, fileHandler });
        server.setHandler(handlers);

    }


    public int getServerPort(){
        return ((ServerConnector)server.getConnectors()[0]).getLocalPort();
    }


    public void stop() throws Exception {
        server.stop();
        server.join();
    }

    public boolean isStarted() {
        return server.isStarted();
    }

    public boolean isStopped() {
        return server.isStopped();
    }

    @Override
    public void run() {
        do{
            try {
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }while(!isStarted());
    }
}
