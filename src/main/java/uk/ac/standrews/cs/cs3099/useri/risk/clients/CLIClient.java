package uk.ac.standrews.cs.cs3099.useri.risk.clients;

import uk.ac.standrews.cs.cs3099.useri.risk.action.Action;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
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
                ret = deployMenu();break;
        }

        return ret;

    }

    private Action deployMenu(){

        Action ret = null;

        if (gameState.getCurrentPlayer().getUnassignedArmy() > 0) {
            System.out.println("You have " + gameState.getCurrentPlayer().getUnassignedArmy() + " unassigned armies. Assign to which country?");

            int selector = 1;

            for (Country c : gameState.getCurrentPlayer().getOccupiedCountries()){
                System.out.println("" + selector++ + " - " + c.getCountryName());
            }

            int country = getChoice(1,selector-1);

            System.out.println("How many armies to deploy to " + gameState.getCurrentPlayer().getOccupiedCountries().get(country-1));

            int amount = getChoice(0, gameState.getCurrentPlayer().getUnassignedArmy());


            //TODO create the deploy action
        }
        else {
            ret = attackMenu();
        }

        return ret;
    }

    private Action attackMenu(){

        Action ret = null;
        System.out.println("ATTACK MENU");
        System.out.println("Select country of origin for attack. In brackets you see (armies,potential targets). select 0 for no attack");


        //DETERMINE POINT OF ORIGIN
        Country origin = selectCountry(gameState.getCurrentPlayer().getOccupiedCountries(),true,false);


        if (origin != null){
            //DETERMINE TARGET

            System.out.println("Select objective for attack. In brackets you see (armies). select 0 for cancelling attack");

            Country objective = selectCountry(origin.getNeighbours(),false,true);

            if (objective != null){

                System.out.println("You have " + origin.getTroops() + " armies in " + origin.getCountryName() + ", there are  " + objective.getTroops() + " armies to defend " + objective.getCountryName() +". How many armies do you attack with?");

                int armies = getChoice(0,origin.getTroops());

                //TODO create attack action


            }
            else {
                ret = attackMenu();
            }

        }

        else {

            ret = fortifyMenu();
        }


        return ret;
    }

    private Action fortifyMenu(){
        System.out.println("FORTIFY MENU");
        return null;
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

    private Country selectCountry(ArrayList<Country> countries, boolean showNeighbours, boolean showCountriesWithoutAttackableNeighbours) {

        ArrayList<Integer> countryMapping = new ArrayList<Integer>();
        countryMapping.add(-1);
        int selector = 1;
        int countryIndex = 0;

        for (Country c : countries) {

            //construct neighbours string
            String neighbours = "";
            for (Country t : c.getNeighbours()) {

                if (t.getOwner() != c.getOwner())
                    neighbours += ", " + t.getCountryName();
            }
            if (neighbours.length() > 0)
                neighbours = neighbours.substring(2);

            if (neighbours.length() > 0 || showCountriesWithoutAttackableNeighbours) {
                System.out.println("" + selector++ + " - " + c.getCountryName() + " (" + c.getTroops() + (showNeighbours ? ", " + neighbours : "") + ")");
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
