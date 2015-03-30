package uk.ac.standrews.cs.cs3099.useri.risk.clients;


import uk.ac.standrews.cs.cs3099.useri.risk.action.Action;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.webClient.JettyServer;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;

import java.awt.*;
import java.net.URI;
import java.net.URL;

public class WebClient extends Client {

    JettyServer jettyServer;
    public WebClient(){
        //Launch Jetty Web Server
        jettyServer = new JettyServer(this);
        jettyServer.run();

        //Wait for it to be ready
        while(!jettyServer.isStarted());

        //Open Web interface in Browser
        int port = jettyServer.getServerPort();
        openWebpage("http://localhost:" + port + "/");
    }

    public static void openWebpage(String urlAsString) {
        try {
            URI uri = new URL(urlAsString).toURI();
            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(uri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            System.out.println("Failed to open WebClient in Local Web Browser");
        }
    }


    /**
     * @return the next action this player takes based on current game state
     */
    @Override
    public Action getAction() {

        return null;
    }

    /**
     * notify player that game state has changed
     */
    @Override
    public void pushGameState() {

    }

    @Override
    public int getDefenders(Country attackingCountry, Country defendingCountry, int attackingArmies) {
        //Auto defend with max troops possible
        return (defendingCountry.getTroops() > 1 ? 2 : 1);
    }

    @Override
    public int[] getSeedComponent() {
        return new int[0];
    }

    @Override
    public boolean isReady(){
        return true;
    }
}
