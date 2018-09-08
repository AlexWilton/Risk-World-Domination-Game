package uk.ac.standrews.cs.cs3099.useri.risk.clients;


import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.CountrySet;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.RandomNumberGenerator;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.*;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * Class to run the game over CLI
 */
public class CLIClient extends Client {

    /**
     * Input Stream for Client
     */
    private InputStream in;

    /**
     * Output Stream for Client
     */
    private PrintStream out;

    /**
     * Constructor for this CLI client. Sets the input and output streams to stdin and stdout.
     * @param gameState The game state to act upon.
     */
    public CLIClient (State gameState) {
        super(gameState, new RandomNumberGenerator());
        this.gameState = gameState;
        this.in = System.in;
        this.out = System.out;
    }

    /**
     * Prints out the tradeable card sets and asks the player to give their choice.
     * @return Trade command or null, depending on player's choice.
     */
    private Command tradeMenu() {
        Command ret = null;
        out.println("TRADE MENU");
        out.println("1 - Trade in Risk Cards");
        out.println("2 - Deployment Stage");

        switch (getChoice(1,2)){
            case 1:
                //TODO poll which cards to trade
                //TODO create trade action
                break;
            case 2:
                gameState.nextStage();
                ret = deployMenu();break;
        }
        return ret;
    }


    /**
     * Asks the player where they want to deploy armies.
     * @return Deploy command or null.
     */
    private Command deployMenu() {
        Command ret;
        if (gameState.getPlayer(playerId).getUnassignedArmies() > 0) {
            out.println("You have " + gameState.getPlayer(playerId).getUnassignedArmies() + " unassigned armies. Assign to which country?");

            Country target = selectCountry(gameState.getPlayer(playerId).getOccupiedCountries(),false,true,false);

            out.println("How many armies to deploy to " + target.getCountryName());

            int amount = getChoice(0, gameState.getPlayer(playerId).getUnassignedArmies());
            ArrayList<DeployTuple> tupList = new ArrayList<>();
            tupList.add(new DeployTuple(target.getCountryId(),amount));
            ret = new DeployCommand(tupList,gameState.getPlayer(playerId).getID());
        }
        else {
            out.println("Skipping deployment - no unassigned armies");
            gameState.nextStage();
            ret = attackMenu();
        }
        return ret;
    }

    /**
     * Asks the player for attack actions.
     * @return Attack Command or null.
     */
    private Command attackMenu(){

        Command ret;
        out.println("ATTACK MENU");
        out.println("Select country of origin for attack. In brackets you see (armies,potential targets). select 0 for no attack");

        //DETERMINE POINT OF ORIGIN
        Country origin = selectCountry(gameState.getPlayer(playerId).getOccupiedCountries(),true,false,false);

        if (origin != null){
            //DETERMINE TARGET
            out.println("Select objective for attack. In brackets you see (armies). select 0 for cancelling attack");

            Country objective = selectCountry(origin.getEnemyNeighbours(),false,true,false);

            if (objective != null){
                out.println("You have " + origin.getTroops() + " armies in " + origin.getCountryName() + ", there are  " + objective.getTroops() + " armies to defend " + objective.getCountryName() +". How many armies do you attack with?");
                int armies = getChoice(0,origin.getTroops());
                ret = new AttackCommand(origin.getCountryId(),objective.getCountryId(),armies,gameState.getPlayer(playerId).getID());
            }
            else {
                ret = attackMenu();
            }
        } else {
            gameState.nextStage();
            ret = fortifyMenu();
        }
        return ret;
    }

    /**
     * Asks the player to fortify their position.
     * @return Fortify command, containing the specified actions, or empty.
     */
    private Command fortifyMenu() {
        Command ret;
        out.println("FORTIFY MENU");
        out.println("If you want to fortify, select the origin country, if you dont, select 0 to end your turn. in brackets you see (available troops, potential targets).");

        Country origin = selectCountry(gameState.getPlayer(playerId).getOccupiedCountries(),false,true,true);

        if (origin != null) {
            out.println("Select the country to fortify, select 0 do choose a different origin. (available troops, potential targets).");
            Country target = selectCountry(origin.getNeighbours(),false,true,true);
            if (target != null) {
                out.println("Select the select the amount of armies to transfer 0 - " + (origin.getTroops()-1) );
                int armies = getChoice(0,origin.getTroops()-1);
                ret = new FortifyCommand(origin.getCountryId(),target.getCountryId(),armies,playerId);
            } else {
                ret = fortifyMenu();
            }
        } else {
            ret = new FortifyCommand(playerId);
        }
        return ret;
    }

    /**
     * Asks the player when they have been attacked, how many armies they want to use for defending.
     * @param attacker The attacking country
     * @param objective The defending country
     * @param attackingArmies The number of armies used for attacking
     * @return The defending dice.
     */
    public int getDefenders(Country attacker, Country objective, int attackingArmies) {
        out.println("" + attacker.getCountryName() + " is attacking " + objective.getCountryName() + " with " + attackingArmies + " armies. You have " + objective.getTroops() + " armies to defend with. Choose how many.");
        return getChoice(1,objective.getTroops());
    }

    @Override
    protected byte[] getSeedComponent() {//empty method to just to replace
        return rng.generateNumber();
    }

    /**
     * Gets the choice the player can make and returns it as an int.
     * @param min minimum number that can be chosen
     * @param max maximum number that can be chosen
     * @return The number chosen.
     */
    private int getChoice(int min, int max) {
        int choice = min-1;
        Scanner inScanner = new Scanner(in);
        while (choice < min || choice > max) {
            out.println("Enter choice:");
            choice = inScanner.nextInt();
            if (choice < min || choice > max){
                out.println("Enter value between " + min + " and " + max + "please!");
            }
        }
        return choice;
    }

    /**
     * Gets the choice from the player for a country from a list of countries available.
     * @param countries List of countries to chose from
     * @param showEnemyNeighbours show the neighbours of the list of countries?
     * @param showCountriesWithoutAttackableNeighbours Show countries without neighbours to be attacked?
     * @param showOwnNeighbours Show neighbours that we own?
     * @return The country selected.
     */
    private Country selectCountry(CountrySet countries, boolean showEnemyNeighbours, boolean showCountriesWithoutAttackableNeighbours, boolean showOwnNeighbours) {
        out.println("-1 - none");
        ArrayList<Integer> sortedKeys = countries.getIDList();
        Collections.sort(sortedKeys);

        for (int key : sortedKeys) {
            String enemyNeighbours = "";
            String ownNeighbours = "";
            Country c = countries.get(key);
            boolean firstItem = true;

            for (Country n : c.getEnemyNeighbours()) {
                if (!firstItem) {
                    enemyNeighbours += ", ";
                } else {
                    firstItem = false;
                }
                enemyNeighbours += n.getCountryName();
            }

            firstItem = true;

            for (Country n : c.getSamePlayerNeighbours()) {
                if (!firstItem) {
                    ownNeighbours += ", ";
                } else {
                    firstItem = false;
                }
                ownNeighbours += n.getCountryName();
            }

            if (showCountriesWithoutAttackableNeighbours || c.getEnemyNeighbours().size() > 0)
                out.println("" + key + " - " + c.getCountryName() + " (" + c.getTroops() + (showEnemyNeighbours ? ", " + enemyNeighbours : "") + (showOwnNeighbours ? ", " + ownNeighbours : "") + ")");
        }

        int choice = getChoice(-1, 50);
        if (choice == -1) {
            return null;
        } else if (!sortedKeys.contains(choice)) {
            out.println("Choose from the list!");
            return selectCountry(countries,showEnemyNeighbours,showCountriesWithoutAttackableNeighbours,showOwnNeighbours);
        } else {
            return countries.get(choice);
        }
    }

    /**
     * Gets the setup command that the player chooses, at the beginning of the game.
     * @return Setup command chosen by the player.
     */
    SetupCommand setupMenu(){
        out.println("SETUP MENU: SELECT COUNTRY TO OCCUPY");
        Country target = selectCountry(gameState.unoccupiedCountries(),true,true,true);
        return new SetupCommand(target.getCountryId(),playerId);
    }

    @Override
    public boolean isReady(){
        return true;
    }

    @Override
    public void pushCommand(Command command) {}

    @Override
    public Command popCommand() {
        Command ret = null;
        if (gameState.hasUnassignedCountries()){
            ret = setupMenu();
        }
        switch (gameState.getTurnStage()){
            case STAGE_TRADING:
                ret = tradeMenu();break;
            case STAGE_DEPLOYING:
                ret = deployMenu();break;
            case STAGE_BATTLES:
                ret = attackMenu();break;
            case STAGE_FORTIFY:
                ret = fortifyMenu();break;
        }
        return ret;
    }

    @Override
    public DefendCommand popDefendCommand(int origin, int target, int armies) {
        int defArmies = getDefenders(gameState.getCountryByID(origin),gameState.getCountryByID(target),armies);
        return new DefendCommand(defArmies,playerId);
    }

    public boolean isLocal(){
        return true;
    }
}