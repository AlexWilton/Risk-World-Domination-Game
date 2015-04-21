package uk.ac.standrews.cs.cs3099.useri.risk.game.action;

import uk.ac.standrews.cs.cs3099.useri.risk.game.*;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.CountrySet;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.RiskCard;

import java.util.ArrayList;

/**
 * Trading in risk cards, at the beginning of the turn.
 * Created by bs44 on 30/01/15.
 */
public class TradeAction extends Action {
    private ArrayList<RiskCard> list;

    public TradeAction(Player player, ArrayList<RiskCard> list) {
        super(player);
        this.list = list;

    }

    /**
     * Validates whether the action can be made against the current game state.
     * No Action is performed.
     *
     * @param state
     * @return true if it is valid
     * false if there is an error
     */
    @Override
    public boolean validateAgainstState(State state) {
        if (super.validateAgainstState(state)) {

            //no trade made!
            if (list == null) {
                return true;
            }
            if (calculateArmies(state) != 0) {
                return true;
            }
        } else {
            System.err.println("out of turn");
        }
        return false;
    }

    /**
     * Performs the action on the game state, alters it accordingly returning the new state
     *
     * @param state
     */
    @Override
    public void performOnState(State state) {
        if (list != null) {
            player.setUnassignedArmies(player.getUnassignedArmies() + calculateArmies(state));

            //set countries which require 2 armies to be deployed. (null if not necessary)
            state.setSetOfCountriesWhereAtLeastOneNeedsToBeDeployedTo(countriesCorrespondingToTradedInRiskCards(state));

            state.cardSettradedIn();
            player.removeCards(list);
        }
        state.nextStage();
    }

    /**
     * Get the countries which are both occupied by the player and also correspond to one of the risk cards currently
     * being traded in.
     * @param state The game state to examine
     * @return
     * null if no card-country pairs are owned by the player
     * list of country-card pairs otherwise.
     */
    private CountrySet countriesCorrespondingToTradedInRiskCards(State state) {
        CountrySet occupied = null;
        for (RiskCard card : list) {
            Country country = state.getCountryByID(card.getCardID());
            if (country.getOwner().equals(player)){
                if(occupied == null) occupied = new CountrySet();
                occupied.add(country);
            }
        }
        return occupied;
    }

    /**
     * Calculate the number of armies and whether the trade action is valid in terms of number of risk cards that are
     * traded in
     * @param state game state
     * @return number of armies to be given to the player or 0 if the move is invalid.
     */
    public int calculateArmies(State state) {
        int numberOfArmies = 0;

        if (list == null || list.size() != 3)
            return 0;
        int cavalry = 0, artillery = 0, infantry = 0, wild = 0;
        for (RiskCard card:list){
            if (card == null)
                return 0;
            switch (card.getType()){
                case TYPE_CAVALRY:
                    cavalry++;
                    break;
                case TYPE_ARTILLERY:
                    artillery++;
                    break;
                case TYPE_INFANTRY:
                    infantry++;
                    break;
                case TYPE_WILDCARD:
                    wild++;
                    break;
            }
        }

        //check if set is valid
        boolean validSet = false;
        if (
                (cavalry == 1 && artillery == 1 && infantry == 1) ||
                cavalry == 3 ||
                infantry == 3 ||
                artillery == 3 ||
                (wild==1 && ((cavalry==2) || (artillery==2) || (infantry==2)))
        ) validSet = true;

        if(validSet) {
            int currentSetNumber = state.getCardSetstradedIn() + 1;
            if (currentSetNumber < 6) {
                numberOfArmies += (currentSetNumber + 1) * 2;
            } else {
                numberOfArmies += (currentSetNumber - 3) * 5;
            }

            //add two extra armies to deploy if one of the risk cards corresponds to an occupied territory
            if(countriesCorrespondingToTradedInRiskCards(state) != null){
                numberOfArmies += 2;
            }

        }

        return numberOfArmies;
    }
}
