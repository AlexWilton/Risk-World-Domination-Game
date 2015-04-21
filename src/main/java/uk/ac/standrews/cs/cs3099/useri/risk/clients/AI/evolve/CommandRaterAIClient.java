package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve;

import org.apache.commons.lang3.ObjectUtils;
import org.json.simple.JSONArray;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.genetic.Gene;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.genetic.Genome;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.game.*;
import uk.ac.standrews.cs.cs3099.useri.risk.game.action.TradeAction;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Continent;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.RiskCard;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.RandomNumberGenerator;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.*;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by po26 on 20/04/15.
 */
public class CommandRaterAIClient extends Client{


        public static int FORTIFY_OLD_ENEMY_NEIGHBOURS_WEIGHT_INDEX = 0;
        public static int FORTIFY_NEW_ENEMY_NEIGHBOURS_WEIGHT_INDEX= 1;
        public static int FORTIFY_ORIGIN_ARMIES_LEFT_INDEX= 2;
        public static int FORTIFY_TARGET_ARMIES_INDEX= 3;
        public static int ATTACK_COUNTRY_ARMIES_WEIGHT_INDEX= 4;
        public static int ATTACK_ENEMY_ARMIES_WEIGHT_INDEX= 5;
        public static int ATTACK_NEW_FRIENDLY_HELPERS_WEIGHT_INDEX= 6;
        public static int ATTACK_NEW_ENEMY_NEIGHBOURS_INDEX= 7;
        public static int DEPLOY_COUNTRY_OPPONENTS_WEIGHT_INDEX= 8;
        public static int DEPLOY_COUNTRY_ARMIES_WEIGHT_INDEX= 9;
        public static int SETUP_OWN_ARMIES_WEIGHT_INDEX= 10;
        public static int SETUP_OPPONENT_NEIGHBOURS_WEIGHT_INDEX= 11;
        public static int SETUP_OWN_NEIGHBOURS_WEIGHT_INDEX= 12;
        public static int CONTINENT_COMPLETION_WEIGHT_INDEX= 13;
        public static int IMPORTANCE_WEIGHT_INDEX= 14;
        public static int MAX_SAME_COUNTRY_DEPLOYMENTS_INDEX= 15;






    private ArrayList<Double> weights;
    private int sameCountryDeployments = 0;
    private int blockedDeploy;


    public Command lastCommand = null;
    public Command lastDeploy = null;
    boolean hasDrawn = false;

    HashMap<Integer, CountryStrategy> strategies = null;

    public CommandRaterAIClient(){
        super(null,new RandomNumberGenerator());
        weights = new ArrayList<>();
        weights.add(-0.5d);
        weights.add(.5d);
        weights.add(1d);
        weights.add(-1d);
        weights.add(1d);
        weights.add(-1d);
        weights.add(.5d);
        weights.add(-.5d);
        weights.add(1d);
        weights.add(-1d);
        weights.add(-1d);
        weights.add(.5d);
        weights.add(-.5d);
        weights.add(.5d);
        weights.add(1d);
        weights.add(5d);


    }

    public CommandRaterAIClient(LinkedList<Gene> genes){
        super(null,new RandomNumberGenerator());
        weights = new ArrayList<>();
        weights.add(((MultiplierGene) genes.get(0)).getValue());
        weights.add(((MultiplierGene) genes.get(1)).getValue());
        weights.add(((MultiplierGene) genes.get(2)).getValue());
        weights.add(((MultiplierGene) genes.get(3)).getValue());
        weights.add(((MultiplierGene) genes.get(4)).getValue());
        weights.add(((MultiplierGene) genes.get(5)).getValue());
        weights.add(((MultiplierGene) genes.get(6)).getValue());
        weights.add(((MultiplierGene) genes.get(7)).getValue());
        weights.add(((MultiplierGene) genes.get(8)).getValue());
        weights.add(((MultiplierGene) genes.get(9)).getValue());
        weights.add(((MultiplierGene) genes.get(10)).getValue());
        weights.add(((MultiplierGene) genes.get(11)).getValue());
        weights.add(((MultiplierGene) genes.get(12)).getValue());
        weights.add(((MultiplierGene)genes.get(13)).getValue());
        weights.add(((MultiplierGene)genes.get(14)).getValue());
        try {
            weights.add((double) ((ConstantVarGene) genes.get(15)).getValue());
        } catch (ClassCastException e){
            System.out.println();
        }

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

    public void reset() {
        sameCountryDeployments = 0;
        blockedDeploy = -1;


        lastCommand = null;
        lastDeploy = null;
        hasDrawn = false;

        strategies = null;
    }

    @Override
    public Command popCommand(){

        if (strategies == null){
            strategies = new HashMap<>();
            for (Country c : gameState.getAllCountries()){
                strategies.put(c.getCountryId(), new CountryStrategy(c, getPlayer(), gameState, 1));
            }
        }

        if (sameCountryDeployments > weights.get(MAX_SAME_COUNTRY_DEPLOYMENTS_INDEX) && lastDeploy != null){
            sameCountryDeployments = 0;
            try {
                blockedDeploy = (Integer.parseInt(((JSONArray) lastDeploy.getPayloadAsArray().get(0)).get(0).toString()));
            } catch (NullPointerException e){
                System.out.println("wrong");
            }
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
        if (commands.size() < 1) {

            for (CountryStrategy strat : strategies.values()){
                RatedCommand rc = rateDeployCommand(strat);
                if (rc != null)
                    commands.add(rc);
            }
        }
    try {
        return commands.get(0).command;
    } catch (IndexOutOfBoundsException e ){
        if(true);
        return null;
    }

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

        if (commands.size() > 0)
            return commands.get(0).command;
        else return null;
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
        double rating = weights.get(IMPORTANCE_WEIGHT_INDEX) * strat.getImportance() + weights.get(SETUP_OWN_ARMIES_WEIGHT_INDEX) * countryArmies +
                weights.get(SETUP_OPPONENT_NEIGHBOURS_WEIGHT_INDEX) * opponentArmiesAround +
                weights.get(SETUP_OWN_NEIGHBOURS_WEIGHT_INDEX) * friendlyArmiesAround +
                calcContinentImportance(strat.country);


        return new RatedCommand(c,rating);
    }


    public RatedCommand rateDeployCommand(CountryStrategy strat){

        DeployCommand c = strat.getBeneficialDeployCommand();
        if (c == null)
            return null;

        JSONArray arr = c.getPayloadAsArray();
        double rating = weights.get(IMPORTANCE_WEIGHT_INDEX) * strat.getImportance() ;

        for (Object row : arr ) {
            Country country = gameState.getCountryByID(Integer.parseInt(((JSONArray) row).get(0).toString()));
            double weight = Integer.parseInt(((JSONArray) row).get(1).toString()) / getPlayer().getUnassignedArmies();
            int countryArmies = country.getTroops();
            int opponentArmiesAround = 0;
            for (Country neighbour : country.getEnemyNeighbours()) {
                opponentArmiesAround += neighbour.getTroops();
            }

            rating += (weight * (weights.get(DEPLOY_COUNTRY_ARMIES_WEIGHT_INDEX)*countryArmies +
                    weights.get(DEPLOY_COUNTRY_OPPONENTS_WEIGHT_INDEX) * opponentArmiesAround))+
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

        double rating = weights.get(IMPORTANCE_WEIGHT_INDEX) * strat.getImportance() +
                weights.get(ATTACK_COUNTRY_ARMIES_WEIGHT_INDEX) * origin.getTroops() +
                weights.get(ATTACK_ENEMY_ARMIES_WEIGHT_INDEX) * target.getTroops() +
                weights.get(ATTACK_NEW_FRIENDLY_HELPERS_WEIGHT_INDEX) * friendlyArmies +
                weights.get(ATTACK_NEW_ENEMY_NEIGHBOURS_INDEX) * enemyArmies+
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


        double rating = weights.get(IMPORTANCE_WEIGHT_INDEX) * strat.getImportance() +
                weights.get(FORTIFY_NEW_ENEMY_NEIGHBOURS_WEIGHT_INDEX) * newEnemyNeighbours +
                weights.get(FORTIFY_OLD_ENEMY_NEIGHBOURS_WEIGHT_INDEX) * oldEnemyNeighbours +
                weights.get(FORTIFY_ORIGIN_ARMIES_LEFT_INDEX) * (origin.getTroops()-armies) +
                weights.get(FORTIFY_TARGET_ARMIES_INDEX) * (target.getTroops()+armies) +
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
       return conti.getCountriesOwnedBy(playerId).size()/conti.getCountries().size() * weights.get(CONTINENT_COMPLETION_WEIGHT_INDEX) * conti.getReinforcementValue();
   }

    public Genome toWeightSet (){
        Genome ret = new Genome();
        for (double d : weights){
            if (Math.abs(d) < 1.001f)
                ret.addGene(new MultiplierGene(d));
            else
                ret.addGene(new ConstantVarGene((int) d));
        }

        return ret;
    }
}
