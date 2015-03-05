package uk.ac.standrews.cs.cs3099.useri.risk.clients.webClient;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyServer implements Runnable {
    private static final String WEB_CLIENT_FILEPATH = "web_client";
    private Server server = null;
    private int serverPort = 5555;

    public JettyServer(){
        server = new Server(serverPort);

        // create the handlers
        Handler dataRequestHandler = new ParamHandler();

        WebAppContext fileHandler = new WebAppContext();
        fileHandler.setContextPath("/");
        fileHandler.setResourceBase(WEB_CLIENT_FILEPATH);

        // create the handler collections
        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[] { dataRequestHandler, fileHandler });
        server.setHandler(handlers);


    }


    public int getServerPort(){
        return serverPort;
    }

    public void start() throws Exception{
        server.start();
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
        try {
            start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
