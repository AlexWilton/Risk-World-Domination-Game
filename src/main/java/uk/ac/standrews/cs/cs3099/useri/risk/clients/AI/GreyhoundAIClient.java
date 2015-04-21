package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI;

import uk.ac.standrews.cs.cs3099.useri.risk.game.*;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.CountrySet;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.RiskCard;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.RandomNumberGenerator;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Always attacks if it can. Will continue attacking one weak country until it is out of armies to do so, or it has conquered the country.
 */
public class GreyhoundAIClient extends AI{


    private AttackCommand lastAttack;

    public GreyhoundAIClient(State gameState){
        super(gameState,new RandomNumberGenerator());
    }




    @Override
    public Command popCommand() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //if we attacked before and haven't won or haven't lost all armies, attack again
        if (lastAttack != null){
            int lastOrigin = Integer.parseInt(lastAttack.getPayloadAsArray().get(0).toString());
            int lastTarget = Integer.parseInt(lastAttack.getPayloadAsArray().get(1).toString());

            if (gameState.isAttackCaptureNeeded()) {
                int armies = gameState.getCountryByID(lastOrigin).getTroops() - 1;
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
        ArrayList<Command> possible = getAllPossibleCommands();
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
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
            ret.addAll(getAllPossibleSetupCommands());
        }else {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
                        RiskCard c = gameState.peekCard();
                        if (c != null)
                            ret.add(new DrawCardCommand(c.getCardID(), playerId));
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

        CountrySet mustDeployTo = gameState.getSetOfCountriesWhereAtLeastOneNeedsToBeDeployedTo();
        DeployTuple tuple = null;

        if (mustDeployTo != null) {
            Country c = mustDeployTo.get(mustDeployTo.getIDList().get(0));

            tuple = new DeployTuple(c.getCountryId(), 2);
            armies_left -= 2;
        }

        for (Country target : allTargets){
            int diff = target.getTroops();
            for (Country opp : target.getEnemyNeighbours()){
                diff -= opp.getTroops();
            }

            troopDiff.put(target.getCountryId(),diff);
        }

        while (armies_left > 0){
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
            troopDiff.put(curr_country,troopDiff.get(curr_country)+1);
            armies_left--;
        }



        ArrayList<DeployTuple> depTups = new ArrayList<>();
        if (tuple != null)
            depTups.add(tuple);

        for (Map.Entry<Integer,Integer> depTuple : troopDeploy.entrySet()){
            if (gameState.getCountryByID(depTuple.getKey()).getOwner().getID() != getPlayerId()){
                System.out.println("WRONG");
            }
            depTups.add(new DeployTuple(depTuple.getKey(),depTuple.getValue()));
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
                if (gameState.getCountryByID(best_neighbour).getOwner().getID() != getPlayerId() || gameState.getCountryByID(curr_country).getOwner().getID() != getPlayerId()){
                    System.out.println("WRONG");
                }
                break;
            }
        }

        if (ret.size() < 1)
            ret.add(new FortifyCommand(playerId));

        return ret;
    }
/*
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
    */
}
