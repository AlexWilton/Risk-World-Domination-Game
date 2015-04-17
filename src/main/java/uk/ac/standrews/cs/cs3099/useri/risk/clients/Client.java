package uk.ac.standrews.cs.cs3099.useri.risk.clients;

import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.RandomNumberGenerator;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.Command;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.DefendCommand;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * represents one client.
 * can take multiple forms, eg local, network or AIClient
 *
 */
public abstract class Client {


    State gameState;

    int playerId;

    private String playerName;

    private byte[] hexSeedComponent;

    private boolean playReady = false;
    private Queue<Command> commandQueue;
    private Queue<String> hashQueue;
    private Queue<String> numberQueue;

    private DefendCommand defendCommand;
    protected RandomNumberGenerator rng;


    protected Client(State gamestate, RandomNumberGenerator rng){

        commandQueue = new ArrayDeque<>();
        hashQueue = new ArrayDeque<>();
        numberQueue = new ArrayDeque<>();
        this.rng = rng;

        this.gameState = gamestate;

    }


    /**
     * notify player that game state has changed
     */
    public abstract int getDefenders(Country attackingCountry, Country defendingCountry, int attackingArmies);

    protected abstract byte[] getSeedComponent();

    public void newSeedComponent(){
        hexSeedComponent = getSeedComponent();
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
        return RandomNumberGenerator.byteToHex(hexSeedComponent);
    }

    public String getHexSeedHash(){
        return RandomNumberGenerator.byteToHex(rng.hashByteArr(hexSeedComponent));
    }

    public String getPlayerName (){
        return playerName;
    }

    public void setPlayerName(String name){
        this.playerName = name;
    }



    public void markPlayReady(){
        this.playReady = true;
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


    public DefendCommand popDefendCommand(int origin, int target, int armies) {
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

    public abstract boolean isLocal();

    public State getState(){
        return gameState;
    }

    public void setState(State gameState){
        this.gameState = gameState;
        System.out.println("Got new State!");
    }

    public void pushRollHash(String rollHash){
        hashQueue.add(rollHash);
    }

    public void pushRollNumber(String rollNumber){
        numberQueue.add(rollNumber);
    }

    public String popRollHash(){
        try {
             while (hashQueue.size() < 1) Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return hashQueue.remove();
    }

    public String popRollNumber(){
        try {
            while (numberQueue.size() < 1) Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return numberQueue.remove();
    }

}
