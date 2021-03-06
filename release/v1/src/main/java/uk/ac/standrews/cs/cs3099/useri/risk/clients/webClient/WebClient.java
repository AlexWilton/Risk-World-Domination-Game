package uk.ac.standrews.cs.cs3099.useri.risk.clients.webClient;


import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.game.TurnStage;
import uk.ac.standrews.cs.cs3099.useri.risk.game.action.ObtainRiskCardAction;
import uk.ac.standrews.cs.cs3099.useri.risk.game.action.SetupAction;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.webClient.JettyServer;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.CountrySet;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.RandomNumberGenerator;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.Command;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.DefendCommand;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.DrawCardCommand;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.SetupCommand;

import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Web Client, running in a browser tab. You can be either a host or join to a host somewhere on the public internet.
 */
public class WebClient extends Client {

    /**
     * Jetty Server for serving Web Client to browser
     */
    private JettyServer jettyServer;

    /**
     * Is this a Web Client for a Host
     */
    private boolean isHost  = false;

    /**
     * If this Web Client is a Host, is it a playing host
     */
    private boolean isPlayingHost = false;

    /**
     * Queue of size one for storing next command to server up to game engine when requested
     */
    private ArrayBlockingQueue<Command> commandQueue = new ArrayBlockingQueue<Command>(1); //can only hold one action at a time.

    /**
     * Construct a Web Client
     */
    public WebClient(){
        super(null, new RandomNumberGenerator());
        //Launch Jetty Web Server
        jettyServer = new JettyServer(this);
        jettyServer.run();

        //Wait for it to be ready
        while(!jettyServer.isStarted());

        //Open Web interface in Browser
        int port = jettyServer.getServerPort();
        openWebpage("http://localhost:" + port + "/");
    }

    /**
     * Open URL in default web browser
     * @param urlAsString URL
     */
    private static void openWebpage(String urlAsString) {
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
     * Put Command in command queue.
     * Waits for queue to be empty. (Blocks)
     * @param command
     */
    public void queueCommand(Command command){
        try {
            commandQueue.put(command);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pop Command from Command Queue
     * @return Command
     */
    @Override
    public Command popCommand() {

        //quick claim counties
        CountrySet uc = gameState.unoccupiedCountries();
        if(uc.size() > 0){
            for(Country c : uc){
                SetupAction sa = new SetupAction(gameState.getPlayer(playerId), c);
                if(sa.validateAgainstState(gameState)) return new SetupCommand(c.getCountryId(), playerId);
            }
        }

        //quick init deploy
        if(gameState.isPreGamePlay()){
            CountrySet ownedCountries = getPlayer().getOccupiedCountries();
            for(Country c : ownedCountries){
                SetupAction sa = new SetupAction(gameState.getPlayer(playerId), c);
                if(sa.validateAgainstState(gameState)) return new SetupCommand(c.getCountryId(), playerId);
            }
        }

        //Deal with STAGE_GET_CARD
        if(gameState.getTurnStage() == TurnStage.STAGE_GET_CARD){
            ObtainRiskCardAction obtainRiskCardAction = new ObtainRiskCardAction(getPlayer());
            if(obtainRiskCardAction.validateAgainstState(gameState))
                return new DrawCardCommand(gameState.peekCard().getCardID(), playerId);
            else
                gameState.nextStage();
        }


        try {
            return commandQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getDefenders(Country attackingCountry, Country defendingCountry, int attackingArmies) {
        //Auto defend with max troops possible
        return (defendingCountry.getTroops() > 1 ? 2 : 1);
    }

    @Override
    protected byte[] getSeedComponent() {
        return rng.generateNumber();
    }

    @Override
    public boolean isReady(){
        return (commandQueue.size() > 0);
    }

    /**
     * Set whether Client is a host and if the host is a playing host
     * @param isHost is client a host
     * @param isPlayingHost is client a playing host
     */
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

    @Override
    public void pushRollHash(String rollHash){

    }
    @Override
    public void pushRollNumber(String rollNumber){

    }
    @Override
    public void pushCommand(Command command) {

    }
}
