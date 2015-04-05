package uk.ac.standrews.cs.cs3099.useri.risk.clients;

import ec.util.MersenneTwisterFast;
import uk.ac.standrews.cs.cs3099.useri.risk.action.*;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.CountrySet;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

public class CLIClient extends Client {

    InputStream in;
    PrintStream out;

    @Override
    public void newSeedComponent() {

    }


    public CLIClient (State gameState){

        this.gameState = gameState;
        this.in = System.in;
        this.out = System.out;
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

    private Action deployMenu(){

        Action ret;

        if (gameState.getCurrentPlayer().getUnassignedArmies() > 0) {
            out.println("You have " + gameState.getCurrentPlayer().getUnassignedArmies() + " unassigned armies. Assign to which country?");

            Country target = selectCountry(gameState.getCurrentPlayer().getOccupiedCountries(),false,true,false);

            out.println("How many armies to deploy to " + target.getCountryName());

            int amount = getChoice(0, gameState.getCurrentPlayer().getUnassignedArmies());


            ret = new DeployArmyAction(gameState.getCurrentPlayer(),target,amount);
        }
        else {
            out.println("Skipping deployment - no unassigned armies");
            gameState.nextStage();
            ret = attackMenu();
        }

        return ret;
    }

    private Action attackMenu(){

        Action ret;
        out.println("ATTACK MENU");
        out.println("Select country of origin for attack. In brackets you see (armies,potential targets). select 0 for no attack");


        //DETERMINE POINT OF ORIGIN
        Country origin = selectCountry(gameState.getCurrentPlayer().getOccupiedCountries(),true,false,false);


        if (origin != null){
            //DETERMINE TARGET

            out.println("Select objective for attack. In brackets you see (armies). select 0 for cancelling attack");

            Country objective = selectCountry(origin.getEnemyNeighbours(),false,true,false);

            if (objective != null){

                out.println("You have " + origin.getTroops() + " armies in " + origin.getCountryName() + ", there are  " + objective.getTroops() + " armies to defend " + objective.getCountryName() +". How many armies do you attack with?");

                int armies = getChoice(0,origin.getTroops());

                //publish attack intention
                //------------- attack is finalised
                //1.get defenders
                int defenders = objective.getOwner().getClient().getDefenders(origin,objective,armies);

                //2. everyone knows the attack plan

                //3. get dice rolls
                RiskDice dice = new RiskDice(RiskDice.ATTACK_ROLL_FACES,armies+defenders);
                //get hashes from all clients
                for (Player p : gameState.getPlayers()){
                    dice.addSeedComponent(p.getClient().getSeedComponent());
                }

                int [] atkDiceRolls = dice.getBattleDiceRolls(0,armies);
                int [] defDiceRolls = dice.getBattleDiceRolls(armies,defenders);

                Arrays.sort(atkDiceRolls);
                Arrays.sort(defDiceRolls);



                out.println("Battle protocol: ");
                out.println("Attacker ( armies ) : " + origin.getCountryName() + " ( " + armies + " )");
                out.println("Defender ( armies ) : " + objective.getCountryName() + " ( " + defenders + " )");
                out.println("Attacker dice rolls  : " + Arrays.toString(atkDiceRolls));
                out.println("Defender dice rolls  : " + Arrays.toString(defDiceRolls));

                ret = new AttackAction(gameState.getCurrentPlayer(),origin,objective,atkDiceRolls,defDiceRolls);


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

    private Action fortifyMenu(){

        Action ret;
        out.println("FORTIFY MENU");
        out.println("If you want to fortify, select the origin country, if you dont, select 0 to end your turn. in brackets you see (available troops, potential targets).");

        Country origin = selectCountry(gameState.getCurrentPlayer().getOccupiedCountries(),false,true,true);

        if (origin != null){
            out.println("Select the country to fortify, select 0 do choose a different origin. (available troops, potential targets).");

            Country target = selectCountry(origin.getNeighbours(),false,true,true);

            if (target != null){
                out.println("Select the select the amount of armies to transfer 0 - " + (origin.getTroops()-1) );

                int armies = getChoice(0,origin.getTroops()-1);

                ret = new FortifyAction(gameState.getCurrentPlayer(),origin,target,armies);
            }
            else{
                ret = fortifyMenu();
            }
        }
        else {
            gameState.nextStage();
            ret = new EndTurnAction(gameState.getCurrentPlayer());
        }


        return ret;
    }

    public int getDefenders(Country attacker, Country objective, int attackingArmies){

        out.println("" + attacker.getCountryName() + " is attacking " + objective.getCountryName() + " with " + attackingArmies + " armies. You have " + objective.getTroops() + " armies to defend with. Choose how many.");

        return getChoice(1,objective.getTroops());
    }

    @Override
    public int[] getSeedComponent() {//empty method to just to replace
        return new int[0];
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
/**
    public int[] getSeedComponent() {
        MersenneTwisterFast twister = new MersenneTwisterFast();
        twister.setSeed(System.currentTimeMillis());
        int [] seedComponent = new int[RiskDice.SEED_ARRAY_LENGTH];
        for (int i = 0; i < RiskDice.SEED_ARRAY_LENGTH; i++){
            seedComponent[i] = twister.nextInt();
        }

        return seedComponent;
    }

    public int[] getSeedHash() {
        MersenneTwisterFast twister = new MersenneTwisterFast();
        twister.setSeed(System.currentTimeMillis());
        int [] seedComponent = new int[RiskDice.SEED_ARRAY_LENGTH];
        for (int i = 0; i < RiskDice.SEED_ARRAY_LENGTH; i++){
            seedComponent[i] = twister.nextInt();
        }

        return seedComponent;
    }
**/
    @Override
    public boolean isReady(){
        return true;
    }
}
