package uk.ac.standrews.cs.cs3099.useri.risk.clients;

import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.Command;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.DefendCommand;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * represents one client.
 * can take multiple forms, eg local, network or AI
 *
 */
public abstract class Client {


    protected State gameState;

    private int playerId;

    private String playerName;

    private String hexSeedComponent;

    private boolean playReady = false;
    private Queue<Command> commandQueue;

    private DefendCommand defendCommand;


    public Client(State gamestate){

        commandQueue = new ArrayDeque<>();

        this.gameState = gamestate;

    }


    /**
     * notify player that game state has changed
     */
    public abstract void pushGameState();

    public abstract int getDefenders(Country attackingCountry, Country defendingCountry, int attackingArmies);

    protected abstract byte[] getSeedComponent();

    public void newSeedComponent(){
        hexSeedComponent = RNGSeed.toHexString(getSeedComponent());
    }

    public void setPlayerId(int playerId){
        this.playerId = playerId;
    }

    public int getPlayerId(){
        return playerId;
    }

    public Player getPlayer() {
        return (gameState == null)? null : gameState.getPlayer(playerId);
    }

    public abstract boolean isReady();

    public String getHexSeed(){
        return hexSeedComponent;
    }

    public String getHexSeedHash(){
        return RNGSeed.hexHashFromHexNumber(getHexSeed());
    }

    public String getPlayerName (){
        return playerName;
    }

    public void setPlayerName(String name){
        this.playerName = name;
    }


    public boolean playReady(){
        return playReady;
    }

    public void markPlayReady(boolean set){
        this.playReady = set;
    }


    public void pushCommand(Command command) {
        if (command instanceof DefendCommand){
            defendCommand=(DefendCommand)command;
        }

        else{
            commandQueue.add(command);
        }
    }


    public Command popCommand() {
        while (commandQueue.isEmpty()){



            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return commandQueue.remove();
    }


    public DefendCommand popDefendCommand() {
        while (defendCommand == null){



            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        DefendCommand ret = defendCommand;

        defendCommand = null;

        return ret;

    }



}
