package uk.ac.standrews.cs.cs3099.useri.risk.clients;

import uk.ac.standrews.cs.cs3099.useri.risk.action.*;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.CountrySet;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by po26 on 30/01/15.
 */
public class CLIClient extends Client {

    public CLIClient (State gameState){
        this.gameState = gameState;
    }
    /**
     * @return the next action this player takes based on current game state
     */
    public Action getAction(){

        Action ret = null;

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

    /**
     * notify player of the
     */
    public void pushGameState(){
        //no need for asynchronous updates (yet)
    }

    private Action tradeMenu(){


        Action ret = null;
        System.out.println("TRADE MENU");
        System.out.println("1 - Trade in Risk Cards");
        System.out.println("2 - Deployment Stage");

        switch (getChoice(1,2)){
            case 1:
                //TODO poll which cards to trade
                //TODO create trade action
                break;
            case 2:
                gameState.nextAction();
                ret = deployMenu();break;
        }

        return ret;

    }

    private Action deployMenu(){

        Action ret;

        if (gameState.getCurrentPlayer().getUnassignedArmy() > 0) {
            System.out.println("You have " + gameState.getCurrentPlayer().getUnassignedArmy() + " unassigned armies. Assign to which country?");

            Country target = selectCountry(gameState.getCurrentPlayer().getOccupiedCountries(),false,true,false);

            System.out.println("How many armies to deploy to " + target.getCountryName());

            int amount = getChoice(0, gameState.getCurrentPlayer().getUnassignedArmy());


            ret = new DeployArmyAction(gameState.getCurrentPlayer(),target,amount);
        }
        else {
            gameState.nextAction();
            ret = attackMenu();
        }

        return ret;
    }

    private Action attackMenu(){

        Action ret;
        System.out.println("ATTACK MENU");
        System.out.println("Select country of origin for attack. In brackets you see (armies,potential targets). select 0 for no attack");


        //DETERMINE POINT OF ORIGIN
        Country origin = selectCountry(gameState.getCurrentPlayer().getOccupiedCountries(),true,false,false);


        if (origin != null){
            //DETERMINE TARGET

            System.out.println("Select objective for attack. In brackets you see (armies). select 0 for cancelling attack");

            Country objective = selectCountry(origin.getNeighbours(),false,true,false);

            if (objective != null){

                System.out.println("You have " + origin.getTroops() + " armies in " + origin.getCountryName() + ", there are  " + objective.getTroops() + " armies to defend " + objective.getCountryName() +". How many armies do you attack with?");

                int armies = getChoice(0,origin.getTroops());


                //get defenders
                int defenders = objective.getOwner().getClient().getDefenders(origin,objective,armies);
                ret = new AttackAction(gameState.getCurrentPlayer(),origin,objective,armies,defenders);


            }
            else {
                ret = attackMenu();
            }

        }

        else {

            gameState.nextAction();
            ret = fortifyMenu();
        }


        return ret;
    }

    private Action fortifyMenu(){

        Action ret;
        System.out.println("FORTIFY MENU");
        System.out.println("If you want to fortify, select the origin country, if you dont, select 0 to end your turn. in brackets you see (available troops, potential targets).");

        Country origin = selectCountry(gameState.getCurrentPlayer().getOccupiedCountries(),false,true,true);

        if (origin != null){
            System.out.println("Select the country to fortify, select 0 do choose a different origin. (available troops, potential targets).");

            Country target = selectCountry(origin.getNeighbours(),false,true,true);

            if (target != null){
                System.out.println("Select the select the amount of armies to transfer 0 - " + (origin.getTroops()-1) );

                int armies = getChoice(0,origin.getTroops()-1);

                ret = new FortifyAction(gameState.getCurrentPlayer(),origin,target,armies);
            }
            else{
                ret = fortifyMenu();
            }
        }
        else{
            gameState.nextAction();
            ret = new EndTurnAction(gameState.getCurrentPlayer());
        }


        return ret;
    }

    public int getDefenders(Country attacker, Country objective, int attackingArmies){

        System.out.println("" + attacker.getCountryName() + " is attacking " + objective.getCountryName() + " with " + attackingArmies + " armies. You have " + objective.getTroops() + " armies to defend with. Choose how many.");

        return getChoice(1,objective.getTroops());
    }

    private int getChoice(int min, int max){

        int choice = min-1;
        Scanner in = new Scanner(System.in);

        while (choice < min || choice > max) {
            System.out.println("Enter choice:");
            choice = in.nextInt();
            if (choice < min || choice > max){
                System.out.println("Enter value between " + min + " and " + max + "please!");
            }
        }

        return choice;
    }

    private Country selectCountry(CountrySet countries, boolean showEnemyNeighbours, boolean showCountriesWithoutAttackableNeighbours, boolean showOwnNeighbours) {

        ArrayList<Integer> countryMapping = new ArrayList<Integer>();
        countryMapping.add(-1);
        int selector = 1;
        int countryIndex = 0;

        for (Country c : countries) {

            //construct neighbours string
            String enemyNeighbours = "";
            String ownNeighbours = "";
            for (Country t : c.getNeighbours()) {

                if (t.getOwner() != c.getOwner())
                    enemyNeighbours += ", " + t.getCountryName();
                else
                    ownNeighbours += ", " + t.getCountryName();
            }
            if (enemyNeighbours.length() > 0)
                enemyNeighbours = enemyNeighbours.substring(2);
            if (ownNeighbours.length() > 0)
                ownNeighbours = ownNeighbours.substring(2);

            if (enemyNeighbours.length() > 0 || showCountriesWithoutAttackableNeighbours) {
                System.out.println("" + selector++ + " - " + c.getCountryName() + " (" + c.getTroops() + (showEnemyNeighbours ? ", " + enemyNeighbours : "") + (showOwnNeighbours ? ", " + ownNeighbours : "")  +  ")");
                countryMapping.add(countryIndex);
            }

            countryIndex++;
        }

        int choice = getChoice(0,selector-1);

        if (choice == 0 )
            return null;
        else
            return countries.get(countryMapping.get(choice));
    }
}
