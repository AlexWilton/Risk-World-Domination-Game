package uk.ac.standrews.cs.cs3099.useri.risk.game.action;

import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.gameModel.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;

import java.util.Arrays;

/**
 * Attacking players. This is optional.
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
    private int attackerArmiesLost = 0;
    private int defenderArmiesLost = 0;


    public AttackAction (Player player, Country attackingCountry, Country defendingCountry, int[] attackerDice, int[] defenderDice) {
        super(player);
        this.attackingCountry = attackingCountry;
        this.defendingCountry = defendingCountry;
        this.attackerDice = attackerDice;
        this.defenderDice = defenderDice;

    }

    public Country getObjective() {
        return defendingCountry;
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
        if(!super.validateAgainstState(state)){
            System.out.println("Error basic");
            return false;
        }


        if(!attackerOK() || !defenderOK())
            return false;

        return attackingCountry.getNeighbours().contains(defendingCountry);

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

        if (defendingCountry.getTroops() < defenderDice.length) {
            System.out.println("Defender doesnt have enough armies");
            return false;
        }

        if (defenderDice.length < MINIMUM_ARMIES)
            return false;

        return defenderDice.length <= MAXIMUM_DEFEND;

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

        return attackerDice.length <= MAXIMUM_ATTACK;

    }

    /**
     * Performs the action on the game state, alters it accordingly. fields attackerLost and
     * defenderLost have to be set for this to do anything.
     *
     * @param state The state to be changed
     */
    @Override
    public void performOnState(State state) {
        // calculate number of armies lost by each player, depending on the dice rolls.
        calculateArmiesLost();

        if (defenderArmiesLost == defendingCountry.getTroops()){ //Attacker won!
            //change owners
            state.territoryCaptured();
            defendingCountry.getOwner().removeCountry(defendingCountry);
            defendingCountry.setOwner(player);
            player.addCountry(defendingCountry);

            //set the number of troops on attacking country.
            attackingCountry.setTroops(attackingCountry.getTroops() - attackerArmiesLost);
            defendingCountry.setTroops(0);

            //tell player it needs to perform an attack capture next
            state.recordAttackCaptureIsNeeded(attackingCountry, defendingCountry, attackerDice.length - attackerArmiesLost);
        }
        else {
            //decrease number of armies on each country, but don't change owner.
            defendingCountry.setTroops(defendingCountry.getTroops() - defenderArmiesLost);
            attackingCountry.setTroops(attackingCountry.getTroops() - attackerArmiesLost);
        }
    }

    /**
     * Calculate the number of armies lost by each player depending on the outcome of the dice rolls. For more
     * information on how exactly this is calculated, look at the slightly obscure code or the risk rules.
     * The dice rolls generally favour the defending player.
     */
    private void calculateArmiesLost() {
        //Sort the arrays in descending order.
        sortDescending(attackerDice);
        sortDescending(defenderDice);
        //if attacker has only one die, see what that was enough for.
        if (attackerDice.length == 1){
            if (attackerDice[0] > defenderDice[0])
                defenderArmiesLost++;
            else
                attackerArmiesLost++;
        }
        //otherwise, the bottleneck is defenderDice.length, so switch on that.
        else {
            switch (defenderDice.length) {
                case 2: //when defender has two dice
                    if (attackerDice[1] > defenderDice[1])
                        defenderArmiesLost++;
                    else
                        attackerArmiesLost++;
                    //the missing break here is deliberate.
                case 1: //when defender has one die
                    if (attackerDice[0] > defenderDice[0]) {
                        defenderArmiesLost++;
                    } else
                        attackerArmiesLost++;
                    break;
            }
        }
    }


    /**
     * Sorts an array of ints in descending order by negating, sorting and negating again.
     * @param array
     */
    private void sortDescending(int[] array) {
        //negate
        array = negation(array);

        Arrays.sort(array);

        //negate
        array = negation(array);
    }

    private int[] negation(int[] arr){
        for(int i : arr) {
            i = -i;
        }
        return arr;
    }
}