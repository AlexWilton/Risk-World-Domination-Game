package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI;

import uk.ac.standrews.cs.cs3099.useri.risk.action.TradeAction;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.game.*;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.RandomNumberGenerator;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Always attacks if it can. Will continue attacking one weak country until it is out of armies to do so, or it has conquered the country.
 */
public class GreatDaneAIClient extends AI{


    private AttackCommand lastAttack;

    public GreatDaneAIClient(State gameState){
        super(gameState,new RandomNumberGenerator());
    }




    @Override
    public Command popCommand() {

        //if we attacked before and haven't won or haven't lost all armies, attack again
        if (lastAttack != null){
            int lastOrigin = Integer.parseInt(lastAttack.getPayloadAsArray().get(0).toString());
            int lastTarget = Integer.parseInt(lastAttack.getPayloadAsArray().get(1).toString());

            if (gameState.isAttackCaptureNeeded()) {
                int armies = gameState.getCountryByID(lastOrigin).getTroops()/2;
                return new AttackCaptureCommand(lastOrigin, lastTarget, armies, playerId);
            }

            if (gameState.getCountryByID(lastTarget).getOwner().getID() != playerId){
                if (gameState.getCountryByID(lastOrigin).getTroops() > gameState.getCountryByID(lastTarget).getTroops()){
                    int atkArmies = gameState.getCountryByID(lastOrigin).getTroops() > 3 ? 3 : (gameState.getCountryByID(lastOrigin).getTroops()-1);
                    //go again
                    return new AttackCommand(lastOrigin,lastTarget,atkArmies,playerId);
                }
            }
        }
        ArrayList<Command> possible = new ArrayList<>();
        while (possible.size()<1)
            possible = getAllPossibleCommands();
        Random r = new Random();
        Command ret = possible.get((int)(r.nextDouble()*possible.size()));
        if (ret instanceof AttackCommand)
            lastAttack = (AttackCommand)ret;
        else
            lastAttack = null;


        return ret;
    }


    @Override
    public int getDefenders(Country attackingCountry, Country defendingCountry, int attackingArmies) {
        //Auto defend with max troops possible
        return (defendingCountry.getTroops() > 1 ? 2 : 1);
    }


    @Override
    public boolean isReady(){
        return true;
    }


    public boolean isLocal(){
        return true;
    }

    @Override
    public DefendCommand popDefendCommand(int origin, int target, int armies) {
        return new DefendCommand((gameState.getCountryByID(target).getTroops() > 1) ? 2 : 1, playerId);
    }


    @Override
    protected byte[] getSeedComponent() {//empty method to just to replace
        return rng.generateNumber();
    }


    private ArrayList<Command> getAllPossibleCommands(){
        ArrayList<Command> ret = new ArrayList<>();


        if (gameState.isPreGamePlay()){
            //only setup commands
            Command bsc = getBestSetupCommand();
            if (bsc != null)
                ret.add(getBestSetupCommand());
        }else {

            TurnStage stage = gameState.getTurnStage();
            switch (stage) {
                case STAGE_TRADING: {
                    ret.addAll(getAllPossiblePlayCardsCommands());
                }
                break;

                case STAGE_DEPLOYING: {
                    ret.addAll(getAllPossibleDeployCommands());
                }
                break;

                case STAGE_BATTLES: {
                    ret.addAll(getAllPossibleAttackCommands());
                } if (ret.size()!=0) break ;//Only break if we cannot attack a weaker country

                case STAGE_GET_CARD: {
                    if (gameState.wonBattle()) {
                        if (gameState.peekCard() != null)
                            ret.add(new DrawCardCommand(gameState.peekCard().getCardID(), playerId));
                    }
                } //NO BREAK, we can go straight to the next stage

                case STAGE_FORTIFY: {
                    ret.addAll(getAllPossibleFortifyCommands());
                }
                break;
                default: {
                    System.out.println("AI problem, unknown turn stage, problem!!");
                }
            }
        }


        return ret;
    }
/*
    private ArrayList<Command> getAllPossiblePlayCardsCommands(){
        ArrayList<Command> ret = new ArrayList<>();
        //can always choose not to play a card
        ret.add(new PlayCardsCommand(playerId));
        //now get all combinations
        for (ArrayList<Integer> combo : getPlayer().getAllValidCardCombinations()) {
            //make Riscard array
            ArrayList<RiskCard> cCombo = new ArrayList<>();
            for (int i : combo){
                cCombo.add(getPlayer().getRiskCardById(i));
            }
            int armies = (new TradeAction(getPlayer(),cCombo)).calculateArmies(gameState);
            ArrayList<ArrayList<Integer>> comboWrapper = new ArrayList<>();
            comboWrapper.add(combo);
            ret.add(new PlayCardsCommand(comboWrapper,armies,playerId));
        }
        return ret;
    }
*/
    private ArrayList<Command> getAllPossibleDeployCommands(){
        //deploy to all boundary countries

        CountrySet allTargets = getPlayer().getOwnedCountriesWithEnemyBoundaries();

        HashMap<Integer,Integer> troopDiff = new HashMap<>();
        HashMap<Integer,Integer> troopDeploy = new HashMap<>();

        int armies_left = getPlayer().getUnassignedArmies();

        for (Country target : allTargets){
            int diff = target.getTroops();
            for (Country opp : target.getEnemyNeighbours()){
                diff -= opp.getTroops();
            }

            troopDiff.put(target.getCountryId(),diff);
        }

        while (troopDeploy.size()<2){
            //find min
            int curr_min = 10000000;
            int curr_country = -1;

            for (Map.Entry<Integer,Integer> troopDiffPair : troopDiff.entrySet()){
                if (troopDiffPair.getValue() <= curr_min){
                    curr_country = troopDiffPair.getKey();
                    curr_min = troopDiffPair.getValue();
                }
            }
            //add deploy
            if (troopDeploy.keySet().contains(curr_country)){
                troopDeploy.put(curr_country,troopDeploy.get(curr_country)+1);
            } else {
                troopDeploy.put(curr_country,1);
            }
            //remove diff
            if (troopDiff.containsKey(curr_country))
                troopDiff.put(curr_country,troopDiff.get(curr_country)+1);

        }



        ArrayList<DeployTuple> depTups = new ArrayList<>();

        int i=0;
        for (Map.Entry<Integer,Integer> depTuple : troopDeploy.entrySet()){
            if (gameState.getCountryByID(depTuple.getKey()) == null){
                ArrayList<Command> ret = new ArrayList<>();
                DeployTuple deptub = new DeployTuple(getPlayer().getOccupiedCountries().get(getPlayer().getOccupiedCountries().getIDList().get(0)).getCountryId(),getPlayer().getUnassignedArmies());
                depTups = new ArrayList<>();
                depTups.add(deptub);
                ret.add(new DeployCommand(depTups,playerId));
                return ret;
            }

            else if (gameState.getCountryByID(depTuple.getKey()).getOwner().getID() != getPlayerId()){
                System.out.println("WRONG");
            }
            depTups.add(new DeployTuple(depTuple.getKey(),((i++)+armies_left)/2));
        }


        ArrayList<Command> ret = new ArrayList<>();
        ret.add(new DeployCommand(depTups,playerId));
        return ret;

    }






    private ArrayList<Command> getAllPossibleAttackCommands(){
        ArrayList<Command> ret = new ArrayList<>();

        for (Country c : getPlayer().getOccupiedCountries()){

            for (Country defender : c.getEnemyNeighbours()){

                //always attack with as many as possible
                int atkArmies = c.getTroops() > 3 ? 3 : (c.getTroops()-1);
                if (atkArmies >= 1 && c.getTroops()>defender.getTroops())
                    ret.add(new AttackCommand(c.getCountryId(),defender.getCountryId(),atkArmies,playerId));
            }
        }
        return ret;
    }

    private ArrayList<Command> getAllPossibleFortifyCommands(){
        ArrayList<Command> ret = new ArrayList<>();
        CountrySet allTargets = getPlayer().getOwnedCountriesWithEnemyBoundaries();
        //Todo improve
        HashMap<Integer,Integer> troopDiff = new HashMap<>();
        HashMap<Integer,Integer> troopDeploy = new HashMap<>();


        for (Country target : allTargets){
            int diff = target.getTroops();
            for (Country opp : target.getEnemyNeighbours()){
                diff -= opp.getTroops();
            }

            troopDiff.put(target.getCountryId(),diff);
        }

        while (troopDiff.size() > 0) {
            //find min
            int curr_min = 10000000;
            int curr_country = -1;

            for (Map.Entry<Integer, Integer> troopDiffPair : troopDiff.entrySet()) {
                if (troopDiffPair.getValue() <= curr_min) {
                    curr_country = troopDiffPair.getKey();
                    curr_min = troopDiffPair.getValue();
                }
            }

            //findBestNeighbour
            int best_neighbour = -1;
            int best_diff = 0;
            for (Country friends : gameState.getCountryByID(curr_country).getSamePlayerNeighbours()){
                int this_diff;
                if (troopDiff.keySet().contains(friends.getCountryId())){
                    this_diff = troopDiff.get(friends.getCountryId());
                }
                else {
                    this_diff = friends.getTroops();
                }

                if (this_diff > best_diff){
                    best_diff = this_diff;
                    best_neighbour = friends.getCountryId();
                }
            }

            if (best_neighbour == -1) {
                troopDiff.remove(curr_country);
                continue;
            }

            else {
                ret.add(new FortifyCommand(best_neighbour,curr_country,gameState.getCountryByID(best_neighbour).getTroops()/2,playerId));
                break;
            }
        }

        if (ret.size() < 1)
            ret.add(new FortifyCommand(playerId));

        return ret;
    }

    private ArrayList<Command> getAllPossibleSetupCommands(){
        ArrayList<Command> ret = new ArrayList<>();

        CountrySet possibleTargets = null;
        if (gameState.hasUnassignedCountries()) {
            possibleTargets = gameState.getAllUnassignedCountries();
        } else {
            possibleTargets = getPlayer().getOccupiedCountries();
        }
        for (Country c : possibleTargets){

            ret.add(new SetupCommand(c.getCountryId(),playerId));
        }
        return ret;
    }

    private Command getBestSetupCommand(){
        /*"continent_names":{
            "0":"North Amercia",
                    "1":"South America",
                    "2":"Europe",
                    "3":"Africa",
                    "4":"Asia",
                    "5":"Australia"
        },*/
        CountrySet possibleLinks = getAllFreeAdjacentCountries();
        if (possibleLinks.size()<1){
            //try to get australia, then southern america, then random
            int[] order = {5,1,3,0,2,4};

            for (int i : order){
                CountrySet free = gameState.getContinentById(i).getUnoccupiedCountries();
                if (free.size() == 0)
                    continue;
                return new SetupCommand(getLowestOpenConnectionCountry(free).getCountryId(),playerId);
            }

        }
        if (gameState.hasUnassignedCountries()) {
            //link to existing countries, by getting the connection that adds the least links


            return new SetupCommand(getLowestOpenConnectionCountry(possibleLinks).getCountryId(), playerId);
        }
            //reinforce indonesia if we have australia
            if (gameState.getContinentById(5).isOwnedBy(playerId) && gameState.getCountryByID(38).getEnemyNeighbours().size() >0){
                return new SetupCommand(38, playerId);
            }
            //reinforce contries with enemy borders
            Country lowest = getPlayer().getOwnedCountriesWithEnemyBoundaries().get(getPlayer().getOwnedCountriesWithEnemyBoundaries().getIDList().get(0));
            for (Country c : getPlayer().getOwnedCountriesWithEnemyBoundaries()){
                if (c.getTroops()<lowest.getTroops())
                    lowest = c;
            }
            return new SetupCommand(lowest.getCountryId(), playerId);





    }

    private Country getLowestOpenConnectionCountry(CountrySet set) {
        Country best = null;
        int lowestConn = 50;
        for (Country c : set){
            int conn = 0;
            for (Country d : c.getNeighbours()) {
                if (d.getOwner() != null) {
                    if (d.getOwner().getID() != playerId) {
                        conn++;
                    }
                }

            }
            if (conn <= lowestConn){
                lowestConn = conn;
                best = c;
            }
        }

        return best;
    }

    private CountrySet getAllFreeAdjacentCountries(){
        CountrySet ret = new CountrySet();
        for (Country c : getPlayer().getOccupiedCountries()){
            for (Country d : c.getNeighbours()){
                if (d.getOwner() == null){
                    ret.add(d);
                }
            }
        }
        return ret;
    }
}
