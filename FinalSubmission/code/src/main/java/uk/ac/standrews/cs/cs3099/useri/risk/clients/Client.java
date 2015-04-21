package uk.ac.standrews.cs.cs3099.useri.risk.clients;

import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.RandomNumberGenerator;

/**
 * represents one client.
 * can take multiple forms, eg local, network or AIClient
 */
public abstract class Client extends CommandQueuer {

    /**
     * Game State
     */
    protected State gameState;

    /**
     * ID of player associated with Client
     */
    protected int playerId;

    /**
     * Random Number Generator
     */
    protected RandomNumberGenerator rng;

    /**
     * Player Name
     */
    private String playerName;

    /**
     * Hex Seed Component
     */
    private byte[] hexSeedComponent;

    /**
     * Is Player Ready
     */
    private boolean playReady = false;

    /**
     * Create Client
     * @param gameState Game State
     * @param rng Random Number Generator
     */
    protected Client(State gameState, RandomNumberGenerator rng){
        this.rng = rng;
        this.gameState = gameState;
    }

    /**
     * notify player that they are under attack.
     */
    public abstract int getDefenders(Country attackingCountry, Country defendingCountry, int attackingArmies);

    /**
     * Calculate seed for random number generator
     * @return
     */
    protected abstract byte[] getSeedComponent();

    /**
     * New Seed Component
     */
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

    public String getHexSeedHash() {
        return RandomNumberGenerator.byteToHex(rng.hashByteArr(hexSeedComponent));
    }

    public String getPlayerName () {
        return playerName;
    }

    public void setPlayerName(String name) {
        this.playerName = name;
    }

    public void markPlayReady() {
        this.playReady = true;
    }

    public abstract boolean isLocal();

    public State getState(){
        return gameState;
    }

    public void setState(State gameState){
        this.gameState = gameState;
    }





}
