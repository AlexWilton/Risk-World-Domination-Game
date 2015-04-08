package uk.ac.standrews.cs.cs3099.useri.risk.clients;


import uk.ac.standrews.cs.cs3099.useri.risk.action.Action;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.webClient.JettyServer;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.TestGameStateFactory;

import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class WebClient extends Client {

    JettyServer jettyServer;
    ArrayBlockingQueue<Action> actionQueue = new ArrayBlockingQueue<Action>(1); //can only hold one action at a time.

    public WebClient(){
        //Launch Jetty Web Server
        jettyServer = new JettyServer(this);
        jettyServer.run();

        //Wait for it to be ready
        while(!jettyServer.isStarted());

        //Open Web interface in Browser
        int port = jettyServer.getServerPort();
        openWebpage("http://localhost:" + port + "/play.html");
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

    public void queueAction(Action action){
        try {
            actionQueue.put(action);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the next action this player takes based on current game state
     */
    @Override
    public Action getAction() {
        try {
            return actionQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        if(actionQueue.size() > 0)
            return true;
        else
            return false;
    }

    public void setState(State gameState){
        synchronized (this) {
            this.gameState = gameState;
            notify();
        }
    }

    public State getState(){
        return gameState;
    }
}
