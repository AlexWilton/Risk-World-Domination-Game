package uk.ac.standrews.cs.cs3099.useri.risk.action;

import uk.ac.standrews.cs.cs3099.useri.risk.game.*;

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
            if (list == null){
                return true;
            }
            if (calculateArmies(state) != 0 ) {
                return true;
            }
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
            //System.err.println("Player has "+player.getUnassignedArmies() + " armies before");
            player.setUnassignedArmies(player.getUnassignedArmies() + calculateArmies(state));
            //System.err.println("Player has "+player.getUnassignedArmies() + " armies after");
            ArrayList<Country> occ;
            if ((occ = occupied(state)) != null) {
                Country x = player.choose(occ);
                x.setTroops(x.getTroops() + 2);
            }
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
    private ArrayList<Country> occupied(State state) {
        ArrayList<Country> occupied = new ArrayList<>();
        boolean set = false;
        for (RiskCard card:list) {
            Country country = state.getCountryByID(card.getCardID());
            if (country.getOwner().equals(player)){
                occupied.add(country);
                set = true;
            }
        }

        if (set)
            return occupied;
        return null;
    }

    /**
     * Calculate the number of armies and whether the trade action is valid in terms of number of risk cards that are
     * traded in
     * @param state game state
     * @return number of armies to be given to the player or 0 if the move is invalid.
     */
    public int calculateArmies(State state) {
        if (list.size() != 3)
            return 0;
        int cavalry = 0;
        int artillery = 0;
        int infantry = 0;
        int wild = 0;
        for (RiskCard card:list){
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
        if (
                (cavalry == 1 && artillery == 1 && infantry == 1) ||
                cavalry == 3 ||
                infantry == 3 ||
                artillery == 3 ||
                (wild==1 && ((cavalry==2) || (artillery==2) || (infantry==2)))
        ) {
            // Increase number of sets traded in.

            int sets = state.getCardSetstradedIn() + 1;
            //System.out.println(sets);
            if (sets < 6){
                return (sets + 1) * 2;
            } else {
                return (sets - 3) * 5;
            }
        }
        return 0;
    }
}
