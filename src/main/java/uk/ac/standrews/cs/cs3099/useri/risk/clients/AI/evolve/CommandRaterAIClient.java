package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve;

import org.json.simple.JSONArray;
import uk.ac.standrews.cs.cs3099.useri.risk.action.AttackAction;
import uk.ac.standrews.cs.cs3099.useri.risk.action.TradeAction;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.RiskCard;
import uk.ac.standrews.cs.cs3099.useri.risk.game.TurnStage;
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

    public Command lastCommand = null;
    boolean hasDrawn = false;

    HashMap<Integer, CountryStrategy> strategies = null;

    public CommandRaterAIClient(){
        super(null,new RandomNumberGenerator());


    }

    @Override
    public int getDefenders(Country attackingCountry, Country defendingCountry, int attackingArmies) {
        return 0;
    }

    @Override
    protected byte[] getSeedComponent() {
        return new byte[0];
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public Command popCommand(){

        if (strategies == null){
            strategies = new HashMap<>();
            for (Country c : gameState.getAllCountries()){
                strategies.put(c.getCountryId(), new CountryStrategy(c, getPlayer(), gameState, 1));
            }
        }

        Command ret = null;

        if (gameState.isAttackCaptureNeeded()){
            //make attack capture
        }
        else if ((lastCommand instanceof AttackCaptureCommand || lastCommand instanceof AttackCommand) && !hasDrawn){
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
            DeployCommand c = strat.getBeneficialDeployCommand();
            commands.add(new RatedCommand(c,rateDeployCommand(c)));
        }

        Collections.sort(commands);

        return commands.get(0).command;
    }

    public Command getBestAttackOrFortifyCommand(){
        ArrayList<RatedCommand> commands = new ArrayList<>();

        for (CountryStrategy strat : strategies.values()){
            AttackCommand c = strat.getBeneficialAttackCommand();
            commands.add(new RatedCommand(c,rateAttackCommand(c)));
            FortifyCommand f = strat.getBeneficialFortifyCommand();
            commands.add(new RatedCommand(f,rateFortifyCommand(f)));
        }

        Collections.sort(commands);

        return commands.get(0).command;
    }

    public Command getBestSetupCommand(){
        ArrayList<RatedCommand> commands = new ArrayList<>();

        for (CountryStrategy strat : strategies.values()){
            SetupCommand c = strat.getBeneficialSetupCommand();
            commands.add(new RatedCommand(c,rateSetupCommand(c)));
        }

        Collections.sort(commands);

        return commands.get(0).command;
    }

    private double rateSetupCommand(SetupCommand c) {
        return 0;
    }

    public double rateDeployCommand(DeployCommand c){
        JSONArray payload = c. getPayloadAsArray();

        double score = 1;
        for (Object line : payload){
            JSONArray triple = (JSONArray) line;

        }

        return 0;
    }

    private double rateAttackCommand(AttackCommand c) {
        return 0;
    }

    private double rateFortifyCommand(FortifyCommand c) {
        return 0;
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
        } else return null;
    }

    private DrawCardCommand getDrawCommand(){
        if (gameState.wonBattle()){
            return new DrawCardCommand(gameState.peekCard().getCardID(),getPlayerId());
        }

        else return null;
    }
}
