package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI;

import uk.ac.standrews.cs.cs3099.useri.risk.game.*;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.RiskCard;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.RandomNumberGenerator;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.*;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by patrick on 17/04/15.
 */
public class ChihuahuaAIClient extends AI{


    public ChihuahuaAIClient(State gameState){
        super(gameState,new RandomNumberGenerator());
    }


    @Override
    public Command popCommand() {

        // This cannot be random, if a country has been captured, move a random number of armies to it (at least 1).
        if (gameState.isAttackCaptureNeeded()) {
            Country origin = gameState.getAttackCaptureOrigin();
            Country destination = gameState.getAttackCaptureDestination();
            int minArmies =  gameState.getAttackCaptureMinimumArmiesToMove();
            int maxArmies = origin.getTroops() - 1;
            Random rn = new Random();
            int n = maxArmies - minArmies + 1;
            int i = Math.abs(rn.nextInt()) % n;
            int armies =  minArmies + i;
            return new AttackCaptureCommand(origin.getCountryId(), destination.getCountryId(), armies, playerId);
        }
        ArrayList<Command> possible = getAllPossibleCommands();
        Random r = new Random();
        return possible.get(r.nextInt(possible.size()));
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
            ret.addAll(getAllPossibleSetupCommands());
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
                }

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
        ArrayList<Command> ret = new ArrayList<>();
        //for now, deploy everything into one country
        int armies = getPlayer().getUnassignedArmies();
        //TODO
        if (getPlayer().getCountryWhichMustBeDeployedTo() != null){
            ArrayList<DeployTuple> tuples = new ArrayList<>();
            tuples.add(new DeployTuple(getPlayer().getCountryWhichMustBeDeployedTo().getCountryId(),armies));
            ret.add(new DeployCommand(tuples,playerId));
            return ret;
        } else {
            for ( Country c : getPlayer().getOccupiedCountries()){
                ArrayList<DeployTuple> tuples = new ArrayList<>();
                tuples.add(new DeployTuple(c.getCountryId(),armies));
                ret.add(new DeployCommand(tuples,playerId));
            }
        }
        return ret;
    }

    private ArrayList<Command> getAllPossibleAttackCommands(){
        ArrayList<Command> ret = new ArrayList<>();

        for (Country c : getPlayer().getOccupiedCountries()){

            for (Country defender : c.getEnemyNeighbours()){

                //always attack with as many as possible
                int atkArmies = c.getTroops() > 3 ? 3 : (c.getTroops()-1);
                if (atkArmies >= 1)
                    ret.add(new AttackCommand(c.getCountryId(),defender.getCountryId(),atkArmies,playerId));
            }
        }
        return ret;
    }

    private ArrayList<Command> getAllPossibleFortifyCommands(){
        ArrayList<Command> ret = new ArrayList<>();
        //no fortification
        ret.add(new FortifyCommand(playerId));
        for (Country c : getPlayer().getOccupiedCountries()){

            for (Country target : c.getSamePlayerNeighbours()){

                for (int i = 1; i<c.getTroops();i++){
                    ret.add(new FortifyCommand(c.getCountryId(),target.getCountryId(),i,playerId));
                }

            }
        }
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
