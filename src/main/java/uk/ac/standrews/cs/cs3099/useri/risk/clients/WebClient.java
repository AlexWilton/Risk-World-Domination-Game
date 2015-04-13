package uk.ac.standrews.cs.cs3099.useri.risk.clients;


import uk.ac.standrews.cs.cs3099.useri.risk.action.Action;
import uk.ac.standrews.cs.cs3099.useri.risk.action.SetupAction;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.webClient.JettyServer;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.CountrySet;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.Command;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.DefendCommand;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.SetupCommand;

import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;

public class WebClient extends Client {

    JettyServer jettyServer;
    boolean isHost  = false;
    boolean isPlayingHost = false;
    ArrayBlockingQueue<Command> commandQueue = new ArrayBlockingQueue<Command>(1); //can only hold one action at a time.

    public WebClient(){
        super(null);
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

    public void queueCommand(Command command){
        try {
            commandQueue.put(command);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Command popCommand() {
        CountrySet uc = gameState.unoccupiedCountries();
        if(uc.size() > 2){
            for(Country c : uc){
                SetupAction sa = new SetupAction(gameState.getPlayer(playerId), c);
                if(sa.validateAgainstState(gameState)) return new SetupCommand(c.getCountryId(), playerId);
            }
        }
        try {
            return commandQueue.take();
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
    protected byte[] getSeedComponent() {
        return RNGSeed.makeRandom256BitNumber();
    }




    @Override
    public boolean isReady(){
        if(commandQueue.size() > 0)
            return true;
        else
            return false;
    }

    public void setHostAndPlayingBooleans(boolean isHost, boolean isPlayingHost){
        this.isHost = isHost;
        this.isPlayingHost = isPlayingHost;
    }




    public boolean isLocal(){
        return true;
    }

    @Override
    public DefendCommand popDefendCommand(int origin, int target, int armies) {
        return new DefendCommand((gameState.getCountryByID(target).getTroops() > 1) ? 2 : 1, playerId);
    }

    public boolean isHost() {
        return isHost;
    }

    public boolean isPlayingHost() {
        return isPlayingHost;
    }
}
