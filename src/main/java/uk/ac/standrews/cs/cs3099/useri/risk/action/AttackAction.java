package uk.ac.standrews.cs.cs3099.useri.risk.action;

import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.game.TurnStage;

/**
 * Attacking players. This is optional.
 * Created by bs44 on 30/01/15.
 */
public class AttackAction extends Action {
    // Minimum and maximum number of armies to attack/defend
    private static final int MINIMUM_ARMIES = 1;
    private static final int MAXIMUM_ATTACK = 3;
    private static final int MAXIMUM_DEFEND = 2;

    private Country attackingCountry;
    private Country defendingCountry;
    private int[] attackerDice;
    private int[] defenderDice;
    private int attackerLost = -99, defenderLost = -99;


    public AttackAction (Player player, Country attackingCountry, Country defendingCountry, int[] attackerDice, int[] defenderDice) {
        super(player, TurnStage.STAGE_BATTLES);
        this.attackingCountry = attackingCountry;
        this.defendingCountry = defendingCountry;
        this.attackerDice = attackerDice;
        this.defenderDice = defenderDice;

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
        if(!super.validateAgainstState(state))
            return false;

        if(!attackerOK() || !defenderOK())
            return false;

        if(!attackingCountry.getNeighbours().contains(defendingCountry))
            return false;

        return true;
    }

    /**
     * Return whether all the conditions for the defender are met (minimum and maximum number of armies and whether the
     * defender has enough armies on the country to defend.
     * @return
     * true if all conditions are met
     * false otherwise.
     */
    private boolean defenderOK() {
        if (defendingCountry.getOwner().equals(player))
            return false; //cannot attack yourself

        if (defendingCountry.getTroops() < defenderDice.length)
            return false;

        if (defenderDice.length < MINIMUM_ARMIES)
            return false;

        if (defenderDice.length > MAXIMUM_DEFEND)
            return false;

        return true;
    }

    /**
     * Return whether all the conditions for the attacker are met (minimum and maximum number of armies and whether the
     * attacker has enough armies on the country to attack.
     * @return
     * true if all conditions are met
     * false otherwise.
     */
    private boolean attackerOK() {
        if (!attackingCountry.getOwner().equals(player))
            return false;

        if (attackingCountry.getTroops() <= attackerDice.length)
            return false;

        if (attackerDice.length < MINIMUM_ARMIES)
            return false;

        if (attackerDice.length > MAXIMUM_ATTACK)
            return false;

        return true;
    }

    /**
     * Performs the action on the game state, alters it accordingly returning the new state. fields attackerLost and
     * defenderLost have to be set for this to do anything.
     *
     * @param state The state to be changed
     */
    @Override
    public void performOnState(State state) {
        int attackerArmiesLost = 0;
        int defenderArmiesLost = 0;

        //compute battle result from dice comparison

        int topDefenderDie = 0;
        for(int dieValue : defenderDice){
            if(dieValue > topDefenderDie)
                topDefenderDie = dieValue;
        }
        int topAttackerDie = 0;
        for(int dieValue : attackerDice){
            if(dieValue > topAttackerDie)
                topAttackerDie = dieValue;
        }
        switch(attackerDice.length) {
            case 1: //when attacker has one die
                if (topAttackerDie > topDefenderDie) {
                    defenderArmiesLost++;
                } else
                    attackerArmiesLost++;
                break;
            case 2: //when attacker has two dice
                if (defenderDice.length == 1){
                    if (topAttackerDie > topDefenderDie)
                        defenderArmiesLost++;
                    else
                        attackerArmiesLost++;
                    }
            //TODO to finish
                break;
            case 3: //when attacker has three dice

                break;
        }

        //TODO write test!!!!
}