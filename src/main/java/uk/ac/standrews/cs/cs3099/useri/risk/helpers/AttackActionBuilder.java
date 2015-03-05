package uk.ac.standrews.cs.cs3099.useri.risk.helpers;

import uk.ac.standrews.cs.cs3099.useri.risk.action.AttackAction;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;

public class AttackActionBuilder {

    int attackerId;
    int defenderId;
    int originId;
    int objectiveId;
    int attackerArmies;
    int defenderArmies;

    int[] attackerRolls;
    int[] defenderRolls;

    public AttackAction buildAction(State gameState) {
        return new AttackAction(gameState.getPlayers().get(attackerId),gameState.getCountryByID(originId),gameState.getCountryByID(objectiveId),attackerRolls,defenderRolls);
    }

    public void setAttackerId(int id){
        attackerId = id;
    }
    public void setDefenderId(int id){
        defenderId = id;
    }
    public void setOriginId(int id){
        originId = id;
    }
    public void setObjectiveId(int id){
        objectiveId = id;
    }
    public void setAttackerArmies(int id){
        attackerArmies = id;
    }
    public void setDefenderArmies(int id){
        defenderArmies = id;
    }

    public void setDefenderRolls(int[] rolls){
        defenderRolls = rolls;
    }
    public void setAttackerRolls(int[] rolls){
        attackerRolls = rolls;
    }


}
