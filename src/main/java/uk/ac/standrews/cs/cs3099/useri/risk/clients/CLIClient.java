package uk.ac.standrews.cs.cs3099.useri.risk.clients;


import uk.ac.standrews.cs.cs3099.risk.game.RandomNumbers;
import uk.ac.standrews.cs.cs3099.useri.risk.action.*;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.CountrySet;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.*;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

public class CLIClient extends Client {

    InputStream in;
    PrintStream out;






    public CLIClient (State gameState){
        super(gameState);
        this.gameState = gameState;
        this.in = System.in;
        this.out = System.out;
    }


    /**
     * notify player of the
     */
    public void pushGameState(){
        //no need for asynchronous updates (yet)
    }

    private Command tradeMenu(){


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

    private Command deployMenu(){

        Command ret;

        if (gameState.getPlayer(playerId).getUnassignedArmies() > 0) {
            out.println("You have " + gameState.getPlayer(playerId).getUnassignedArmies() + " unassigned armies. Assign to which country?");

            Country target = selectCountry(gameState.getPlayer(playerId).getOccupiedCountries(),false,true,false);

            out.println("How many armies to deploy to " + target.getCountryName());

            int amount = getChoice(0, gameState.getPlayer(playerId).getUnassignedArmies());


            ret = new DeployCommand(target.getCountryId(),amount,gameState.getPlayer(playerId).getID());
        }
        else {
            out.println("Skipping deployment - no unassigned armies");
            gameState.nextStage();
            ret = attackMenu();
        }

        return ret;
    }

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

        }

        else {

            gameState.nextStage();
            ret = fortifyMenu();
        }


        return ret;
    }

    private Command fortifyMenu(){

        Command ret;
        out.println("FORTIFY MENU");
        out.println("If you want to fortify, select the origin country, if you dont, select 0 to end your turn. in brackets you see (available troops, potential targets).");

        Country origin = selectCountry(gameState.getPlayer(playerId).getOccupiedCountries(),false,true,true);

        if (origin != null){
            out.println("Select the country to fortify, select 0 do choose a different origin. (available troops, potential targets).");

            Country target = selectCountry(origin.getNeighbours(),false,true,true);

            if (target != null){
                out.println("Select the select the amount of armies to transfer 0 - " + (origin.getTroops()-1) );

                int armies = getChoice(0,origin.getTroops()-1);

                ret = new FortifyCommand(origin.getCountryId(),target.getCountryId(),armies,playerId);
            }
            else{
                ret = fortifyMenu();
            }
        }
        else {

            ret = new FortifyCommand(playerId);
        }


        return ret;
    }

    public int getDefenders(Country attacker, Country objective, int attackingArmies){

        out.println("" + attacker.getCountryName() + " is attacking " + objective.getCountryName() + " with " + attackingArmies + " armies. You have " + objective.getTroops() + " armies to defend with. Choose how many.");

        return getChoice(1,objective.getTroops());
    }

    @Override
    protected byte[] getSeedComponent() {//empty method to just to replace
        return RNGSeed.makeRandom256BitNumber();
    }

    private int getChoice(int min, int max){

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
        }
        else if (!sortedKeys.contains(choice)){
            out.println("Choose from the list!");
            return selectCountry(countries,showEnemyNeighbours,showCountriesWithoutAttackableNeighbours,showOwnNeighbours);
        }
            return countries.get(choice);
    }

    public void setStreams(InputStream in, PrintStream out){
        this.in = in;
        this.out = out;
    }



    public SetupCommand setupMenu(){
        out.println("SETUP MENU: SELECT COUNTRY TO OCCUPY");
        Country target = selectCountry(gameState.unoccupiedCountries(),true,true,true);

        return new SetupCommand(target.getCountryId(),playerId);
    }

    @Override
    public boolean isReady(){
        return true;
    }



    @Override
    public void pushCommand(Command command) {

    }

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
