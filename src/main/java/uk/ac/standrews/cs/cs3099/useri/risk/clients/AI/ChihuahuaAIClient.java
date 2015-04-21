package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI;

import uk.ac.standrews.cs.cs3099.useri.risk.game.*;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.CountrySet;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.RiskCard;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.RandomNumberGenerator;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.*;

import java.util.ArrayList;
import java.util.Random;

/**
 * Chihuahua AI Client
 * Always performs a Random valid move.
 */
public class ChihuahuaAIClient extends AI{

    /**
     * Create a Chihuahua AI Client
     * @param gameState Game State
     */
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

    /**
     * Collects a list of all possible commands the AI can make
     * @return A list of all possible commands the AI can make
     */
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

    /**
     * Collects a list of all possible deploy commands the AI can make
     * @return A list of all possible deploy commands the AI can make
     */
    private ArrayList<Command> getAllPossibleDeployCommands(){
        ArrayList<Command> ret = new ArrayList<>();
        //for now, deploy everything into one country
        int armies = getPlayer().getUnassignedArmies();
        CountrySet mustDeployTo = gameState.getSetOfCountriesWhereAtLeastOneNeedsToBeDeployedTo();
        DeployTuple tuple = null;

        if (mustDeployTo != null) {
            Country c = mustDeployTo.get(mustDeployTo.getIDList().get(0));

            tuple = new DeployTuple(c.getCountryId(), 2);
            armies -= 2;
        }

        for ( Country c1 : getPlayer().getOccupiedCountries()){
            ArrayList<DeployTuple> tuples = new ArrayList<>();
            if (tuple != null)
                tuples.add(tuple);
            tuples.add(new DeployTuple(c1.getCountryId(),armies));
            ret.add(new DeployCommand(tuples,playerId));
        }

        return ret;
    }

    /**
     * Collects a list of all possible attack commands the AI can make
     * @return A list of all possible attack commands the AI can make
     */
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

    /**
     * Collects a list of all possible fortify commands the AI can make
     * @return A list of all possible fortify commands the AI can make
     */
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
}
