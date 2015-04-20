package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI;

import uk.ac.standrews.cs.cs3099.useri.risk.game.action.TradeAction;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.CountrySet;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.RiskCard;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.RandomNumberGenerator;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.Command;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.PlayCardsCommand;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.SetupCommand;

import java.util.ArrayList;

/**
 * Abstract class for representation an AI Client
 */
public abstract class AI extends Client {

    /**
     * Abstract Constructor for AI
     * @param gamestate Game State
     * @param rnd Random Number Generator
     */
    public AI(State gamestate, RandomNumberGenerator rnd){
            super(gamestate,rnd);
        }

    /**
     * Auxilary method abstracted for getting all possible play card commands
     */
    protected ArrayList<Command> getAllPossiblePlayCardsCommands(){
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

    /**
     *  Auxilary method abstracted for getting all possible setup commands
     * @return Possible setup commands
     */
    protected ArrayList<Command> getAllPossibleSetupCommands(){
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
}
