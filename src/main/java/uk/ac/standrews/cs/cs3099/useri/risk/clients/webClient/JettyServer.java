package uk.ac.standrews.cs.cs3099.useri.risk.clients.webClient;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;

public class JettyServer implements Runnable {
    private static final String WEB_CLIENT_FILEPATH = "web_client";
    private Server server = null;
    private int serverPort = 5555;

    public JettyServer(){
        setupServer();
    }

    private void setupServer() {
        server = new Server();
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(serverPort);
        server.addConnector(connector);

        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{"index.html"});

        resource_handler.setResourceBase(WEB_CLIENT_FILEPATH);

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler, new DefaultHandler() });
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
