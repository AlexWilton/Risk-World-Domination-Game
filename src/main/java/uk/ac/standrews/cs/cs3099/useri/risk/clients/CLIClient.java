package uk.ac.standrews.cs.cs3099.useri.risk.clients;

import uk.ac.standrews.cs.cs3099.useri.risk.action.Action;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;

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
                //TODO
                break;
            case 2:
                ret = deployMenu();break;
        }

        return ret;

    }

    private Action deployMenu(){

        Action ret = null;

        if (gameState.getCurrentPlayer().getUnassignedArmy() > 0) {
            System.out.println("You have " + gameState.getCurrentPlayer().getUnassignedArmy() + " unassigend armies. Assign to which country?");

            int selector = 1;

            for (Country c : gameState.getCurrentPlayer().getOccupiedCountries()){
                System.out.println("" + selector++ + " - " + c.getCountryName());
            }

            int country = getChoice(1,selector-1);

            switch (getChoice(1, 2)) {
                case 1:
                    //TODO
                    break;
                case 2:
                    ret = deployMenu();
                    break;
            }


        }
        else {
            ret = attackMenu();
        }

        return ret;
    }

    private Action attackMenu(){
        System.out.println("ATTACK MENU");
        return null;
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
}
