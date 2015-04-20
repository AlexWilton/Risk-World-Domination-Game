package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve;

import org.json.simple.JSONArray;
import uk.ac.standrews.cs.cs3099.useri.risk.action.AttackAction;
import uk.ac.standrews.cs.cs3099.useri.risk.action.TradeAction;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.game.*;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.RandomNumberGenerator;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by po26 on 20/04/15.
 */
public class CommandRaterAIClient extends Client{

    double fortifyOldEnemyNeighboursWeight = -0.5f;
    double fortifyNewEnemyNeighboursWeight = .5f;
    double fortifyOriginArmiesLeft = 1;
    double fortifyTargetArmies = -1;
    double attackCountryArmiesWeight = 1;
    double attackEnemyArmiesWeight = -1;
    double attackNewFriendlyHelpersWeight = .5f;
    double attackNewEnemyNeighbours = -.5f;
    double deployCountryOpponentsWeight = 1;
    double deployCountryArmiesWeight = -1;
    double setupOwnArmiesWeight = -1;
    double setupOpponentNeighboursWeight = .5f;
    double setupOwnNeighboursWeight = -.5;
    double continentCompletionWeight = 0.5f;
    double importanceWeight = 1;

    double maxSameCountryDeployments = 5;




    private int sameCountryDeployments = 0;
    private int blockedDeploy;


    public Command lastCommand = null;
    public Command lastDeploy = null;
    boolean hasDrawn = false;

    HashMap<Integer, CountryStrategy> strategies = null;

    public CommandRaterAIClient(){
        super(null,new RandomNumberGenerator());


    }

    @Override
    public int getDefenders(Country attackingCountry, Country defendingCountry, int attackingArmies) {
        return (defendingCountry.getTroops() > 1 ? 2 : 1);
    }

    @Override
    public DefendCommand popDefendCommand(int origin, int target, int armies) {
        return new DefendCommand((gameState.getCountryByID(target).getTroops() > 1) ? 2 : 1, playerId);
    }


    @Override
    protected byte[] getSeedComponent() {//empty method to just to replace
        return rng.generateNumber();
    }


    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public Command popCommand(){

        if (strategies == null){
            strategies = new HashMap<>();
            for (Country c : gameState.getAllCountries()){
                strategies.put(c.getCountryId(), new CountryStrategy(c, getPlayer(), gameState, 1));
            }
        }

        if (sameCountryDeployments > maxSameCountryDeployments){
            sameCountryDeployments = 0;
            blockedDeploy = (Integer.parseInt(((JSONArray)lastDeploy.getPayloadAsArray().get(0)).get(0).toString()));
        }


        Command ret = null;

        if (gameState.isAttackCaptureNeeded()){
            ret = getBestAttackCaptureCommand();
        }
        else if ((lastCommand instanceof AttackCaptureCommand || lastCommand instanceof AttackCommand) && !hasDrawn && gameState.peekCard() != null){
            //draw card
            if (gameState.wonBattle()) {
                ret = getDrawCommand();
            }
        }
        else if (gameState.isPreGamePlay()){
            //only setup commands
            ret = getBestSetupCommand();
        }else {

            TurnStage stage = gameState.getTurnStage();
            switch (stage) {

                case STAGE_TRADING: {
                    ret = getPlayCardsCommand();
                    hasDrawn = false;
                } break;

                case STAGE_DEPLOYING: {
                    ret = getBestDeployCommand();
                    if (lastDeploy != null) {
                        if (Integer.parseInt(((JSONArray) ret.getPayloadAsArray().get(0)).get(0).toString()) == Integer.parseInt(((JSONArray) lastDeploy.getPayloadAsArray().get(0)).get(0).toString())) {
                            sameCountryDeployments++;
                        }
                    }
                    lastDeploy = ret;
                } break;

                case STAGE_BATTLES:
                case STAGE_GET_CARD:
                case STAGE_FORTIFY:{
                    ret = getBestAttackOrFortifyCommand();

                } break;

                default: {
                    System.out.println("AI problem, unknown turn stage, problem!!");
                }
            }
        }
        if (ret == null)
            ret = new FortifyCommand(playerId);

        lastCommand = ret;
        return ret;
    }

    public Command getBestDeployCommand(){
        ArrayList<RatedCommand> commands = new ArrayList<>();

        for (CountryStrategy strat : strategies.values()){
            RatedCommand rc = rateDeployCommand(strat);
            if (rc != null && Integer.parseInt(((JSONArray)rc.command.getPayloadAsArray().get(0)).get(0).toString()) != blockedDeploy)
                commands.add(rc);
        }

        Collections.sort(commands);

        return commands.get(0).command;
    }

    public Command getBestAttackCaptureCommand() {
        //make attack capture
        JSONArray payload = lastCommand.getPayloadAsArray();
        Country origin = gameState.getCountryByID(Integer.parseInt(payload.get(0).toString()));
        Country target = gameState.getCountryByID(Integer.parseInt(payload.get(1).toString()));
        int armies = Integer.parseInt(payload.get(2).toString());
        int enemyArmiesAroundOrig = 0;
        for (Country c : origin.getNeighboursNotOwnedBy(playerId)) {
            enemyArmiesAroundOrig += c.getTroops();
        }
        int enemyArmiesAroundTarget = 0;
        for (Country c : target.getEnemyNeighbours()) {
            enemyArmiesAroundTarget += c.getTroops();
        }

        if (enemyArmiesAroundOrig != 0){
            double division = ((double) enemyArmiesAroundTarget) / ((double) enemyArmiesAroundOrig + enemyArmiesAroundTarget);
            int redepArmies = (int) (division * ((double) origin.getTroops()));
            if (redepArmies >= origin.getTroops()) {
                redepArmies = origin.getTroops() -1;
            }
            if (redepArmies < armies)
                redepArmies = armies;

            return new AttackCaptureCommand(origin.getCountryId(),target.getCountryId(),redepArmies,playerId);
        }
        return new AttackCaptureCommand(origin.getCountryId(),target.getCountryId(),origin.getTroops() -1,playerId);
    }

    public Command getBestAttackOrFortifyCommand(){
        ArrayList<RatedCommand> commands = new ArrayList<>();

        for (CountryStrategy strat : strategies.values()){

            RatedCommand rc = rateAttackCommand(strat);
            if (rc != null)
                commands.add(rc);

            RatedCommand rc1 = rateFortifyCommand(strat);
            if (rc1 != null)
                commands.add(rc1);
        }

        Collections.sort(commands);

        return commands.get(0).command;
    }

    public Command getBestSetupCommand(){
        ArrayList<RatedCommand> commands = new ArrayList<>();

        for (CountryStrategy strat : strategies.values()){
            RatedCommand rc = rateSetupCommand(strat);
            if (rc != null)
                commands.add(rc);
        }

        Collections.sort(commands);

        return commands.get(0).command;
    }


    private RatedCommand rateSetupCommand(CountryStrategy strat) {

        SetupCommand c = strat.getBeneficialSetupCommand();
        if (c == null)
            return null;
        Country country = gameState.getCountryByID(c.getPayloadAsInt());
        int countryArmies = country.getTroops();
        int opponentArmiesAround = 0;
        for (Country neighbour : country.getEnemyNeighbours()){
            opponentArmiesAround += neighbour.getTroops();
        }
        int friendlyArmiesAround = 0;
        for (Country neighbour : country.getEnemyNeighbours()){
            friendlyArmiesAround += neighbour.getTroops();
        }
        double rating = importanceWeight * strat.getImportance() + setupOwnArmiesWeight * countryArmies +
                setupOpponentNeighboursWeight * opponentArmiesAround +
                setupOwnNeighboursWeight * friendlyArmiesAround +
                calcContinentImportance(strat.country);


        return new RatedCommand(c,rating);
    }


    public RatedCommand rateDeployCommand(CountryStrategy strat){

        DeployCommand c = strat.getBeneficialDeployCommand();
        if (c == null)
            return null;

        JSONArray arr = c.getPayloadAsArray();
        double rating = importanceWeight * strat.getImportance() ;

        for (Object row : arr ) {
            Country country = gameState.getCountryByID(Integer.parseInt(((JSONArray) row).get(0).toString()));
            double weight = Integer.parseInt(((JSONArray) row).get(1).toString()) / getPlayer().getUnassignedArmies();
            int countryArmies = country.getTroops();
            int opponentArmiesAround = 0;
            for (Country neighbour : country.getEnemyNeighbours()) {
                opponentArmiesAround += neighbour.getTroops();
            }

            rating += (weight * (deployCountryArmiesWeight*countryArmies +
                    deployCountryOpponentsWeight * opponentArmiesAround))+
                    calcContinentImportance(strat.country);
        }




        return new RatedCommand(c,rating);
    }



    private RatedCommand rateAttackCommand(CountryStrategy strat) {
        AttackCommand c = strat.getBeneficialAttackCommand();
        if (c == null)
            return null;
        JSONArray payload = c.getPayloadAsArray();
        Country origin = gameState.getCountryByID(Integer.parseInt(payload.get(0).toString()));
        Country target = gameState.getCountryByID(Integer.parseInt(payload.get(1).toString()));



        int friendlyArmies = 0;
        for (Country neighbour : target.getNeighboursOwnedBy(playerId)){
            friendlyArmies += neighbour.getTroops();
        }

        int enemyArmies = 0;
        for (Country neighbour : target.getNeighboursOwnedBy(target.getOwner().getID())){
            enemyArmies += neighbour.getTroops();
        }

        double rating = importanceWeight * strat.getImportance() +
                attackCountryArmiesWeight * origin.getTroops() +
                attackEnemyArmiesWeight * target.getTroops() +
                attackNewFriendlyHelpersWeight * friendlyArmies +
                attackNewEnemyNeighbours * enemyArmies+
                calcContinentImportance(strat.country);


        return new RatedCommand(c,rating);
    }

    private RatedCommand rateFortifyCommand(CountryStrategy strat) {

        FortifyCommand c = strat.getBeneficialFortifyCommand();
        if (c == null)
            return null;

        JSONArray payload = c.getPayloadAsArray();
        Country origin = gameState.getCountryByID(Integer.parseInt(payload.get(0).toString()));
        Country target = gameState.getCountryByID(Integer.parseInt(payload.get(1).toString()));
        int armies = Integer.parseInt(payload.get(2).toString());



        int oldEnemyNeighbours = 0;
        for (Country neighbour : origin.getEnemyNeighbours()){
            oldEnemyNeighbours += neighbour.getTroops();
        }

        int newEnemyNeighbours = 0;
        for (Country neighbour : target.getEnemyNeighbours()){
            newEnemyNeighbours += neighbour.getTroops();
        }


        double rating = importanceWeight * strat.getImportance() +
                fortifyNewEnemyNeighboursWeight * newEnemyNeighbours +
                fortifyOldEnemyNeighboursWeight * oldEnemyNeighbours +
                fortifyOriginArmiesLeft * (origin.getTroops()-armies) +
                fortifyTargetArmies * (target.getTroops()+armies)-100 +
        calcContinentImportance(strat.country);




        return new RatedCommand(c,rating);
    }

    private PlayCardsCommand getPlayCardsCommand(){
        ArrayList<ArrayList<Integer>> possibleCombos = getPlayer().getAllValidCardCombinations();

        TradeAction best = null;
        ArrayList<Integer> ret = null;

        for (ArrayList<Integer> triple : possibleCombos){
            ArrayList<RiskCard> cards = new ArrayList<>();
            for (int id : triple){
                cards.add(getPlayer().getRiskCardById(id));
            }
            TradeAction ta = new TradeAction(getPlayer(),cards);
            if (best == null) {
                best = ta;
                ret = triple;
            }
            if (ta.calculateArmies(gameState) > best.calculateArmies(gameState)){
                best = ta;
                ret = triple;
            }
        }

        if (ret != null) {
            ArrayList<ArrayList<Integer>> retWrap = new ArrayList<>();
            retWrap.add(ret);

            return new PlayCardsCommand(retWrap, best.calculateArmies(gameState), getPlayerId());
        } else return new PlayCardsCommand(playerId);
    }

    private DrawCardCommand getDrawCommand(){
        if (gameState.wonBattle()){
            if (gameState.peekCard() != null)
                return new DrawCardCommand(gameState.peekCard().getCardID(),getPlayerId());
            else return null;
        }

        else return null;
    }

   private double calcContinentImportance(Country c ){
       Continent conti = gameState.getCountryContinent(c.getCountryId());
       return conti.getCountriesOwnedBy(playerId).size()/conti.getCountries().size() * continentCompletionWeight * conti.getReinforcementValue();
   }
}
