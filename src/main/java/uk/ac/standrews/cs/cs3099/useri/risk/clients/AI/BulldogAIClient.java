package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI;

import uk.ac.standrews.cs.cs3099.useri.risk.action.TradeAction;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.game.*;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.RandomNumberGenerator;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.*;

import java.util.ArrayList;
import java.util.Random;

/**
 * Always attacks if it can. Will continue attacking one weak country until it is out of armies to do so, or it has conquered the country.
 */
public class BulldogAIClient extends AI{


    private AttackCommand lastAttack;

    public BulldogAIClient(State gameState){
        super(gameState,new RandomNumberGenerator());
    }




    @Override
    public Command popCommand() {

        //if we attacked before and havent won or havent lost all armies, attack again
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
        Command ret = possible.get((int)(r.nextDouble()*possible.size()));;
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
                } if (ret.size()!=0) break ;//Only break if we cannot attack a weaker country

                case STAGE_GET_CARD: {
                    if (gameState.wonBattle()) {
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
                if (atkArmies >= 1 && c.getTroops()>defender.getTroops())
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
