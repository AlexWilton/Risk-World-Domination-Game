package uk.ac.standrews.cs.cs3099.useri.risk.action;

import uk.ac.standrews.cs.cs3099.useri.risk.game.Country;
import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.game.TurnStage;

/**
 * Voluntary
 * Created by bs44 on 30/01/15.
 */
public class FortifyAction extends Action {
    private Country from;
    private Country to;
    private int armies;

    private boolean isEndTurnOnly;

    public FortifyAction(Player player, Country from, Country to, int armies) {
        super(player, TurnStage.STAGE_FORTIFY);
        this.from = from;
        this.to = to;
        this.armies = armies;
    }

    public FortifyAction(Player player){
        super(player,TurnStage.STAGE_FORTIFY);
        isEndTurnOnly = true;
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
        if (isEndTurnOnly == true){
            return true;
        }
        if (super.validateAgainstState(state)){
            if (validMove()) {
                return true;
            }
        }
        return false;
    }

    private boolean validMove(){
        if (from.getTroops() <= armies)
            return false;
        if (! from.getOwner().equals(player))
            return false;
        if (! to.getOwner().equals(player))
            return false;
        return true;
    }

    /**
     * Performs the action on the game state, alters it accordingly returning the new state
     *
     * @param state
     */
    @Override
    public void performOnState(State state) {
        if (!isEndTurnOnly) {
            from.setTroops(from.getTroops() - armies);
            to.setTroops(to.getTroops() + armies);
            state.nextStage();
        }
        //Fortify HAS TO END TURN ACCORDING TO PROTOCOL
        state.endTurn();
    }
}
