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

    private final int attack;
    private final int defend;
    private final Player defender;
    private Country from, to;
    private int attackerLost = -99, defenderLost = -99;


    public AttackAction (Player player, Country from, Country to, int attack, int defend) {
        super(player, TurnStage.STAGE_BATTLES);
        this.from = from;
        this.to = to;
        this.attack = attack;
        this.defend = defend;
        this.defender = to.getOwner();
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
            if (attackerOK() && defenderOK()) {
                if (from.getNeighbours().contains(to)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return whether all the conditions for the defender are met (minimum and maximum number of armies and whether the
     * defender has enough armies on the country to defend.
     * @return
     * true if all conditions are met
     * false otherwise.
     */
    private boolean defenderOK() {
        if (to.getOwner().equals(player))
            return false;
        if (to.getTroops() < defend)
            return false;
        if (defend < MINIMUM_ARMIES)
            return false;
        if (defend > MAXIMUM_DEFEND)
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
        if (! from.getOwner().equals(player))
            return false;
        if (from.getTroops() <= attack)
            return false;
        if (attack < MINIMUM_ARMIES)
            return false;
        if (attack > MAXIMUM_ATTACK)
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
        // TODO Should the action call back to the player, or should the player hand over result of dice rolls?
        // currently the player (or someone) has to set the results, otherwise the action does nothing.
        if (defenderLost == -99 || attackerLost == -99){
            return;
        }
        if (defenderLost == to.getTroops()) {
            // The defender lost the country.
            state.winning();
            from.setTroops(from.getTroops() - attack);
            to.setTroops(attack);
            to.getOwner().removeCountry(to);
            player.addCountry(to);
            to.setOwner(player);
        }
        else {
            from.setTroops(from.getTroops() - attackerLost);
            to.setTroops(to.getTroops() - defenderLost);
        }
        //TODO write tests!
    }

    public void setBattleResults(int attackerLost, int defenderLost) {
        // do not set these twice!
        if (this.attackerLost != -99 || this.defenderLost != -99)
            return;
        this.attackerLost = attackerLost;
        this.defenderLost = defenderLost;
    }
}