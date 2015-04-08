package uk.ac.standrews.cs.cs3099.useri.risk.helpers;

import uk.ac.standrews.cs.cs3099.risk.game.RandomNumbers;
import uk.ac.standrews.cs.cs3099.useri.risk.action.AttackAction;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.NetworkClient;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.RNGSeed;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;

public class AttackActionBuilder implements Runnable{

    int attackerId;
    int defenderId;
    int originId;
    int objectiveId;
    int attackerArmies;
    int defenderArmies;

    int[] attackerRolls;
    int[] defenderRolls;

    State gameState;
    ClientSocketHandler clientSocketHandler;

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

    public void setGamesState(State state) {
        gameState = state;
    }

    public void setClientSocketHandler(ClientSocketHandler csh){
        clientSocketHandler = csh;
    }

    @Override
    public void run(){
        //wait for seed
        RNGSeed seed = clientSocketHandler.popSeed();

        //make rng
        RandomNumbers rng = new RandomNumbers(seed.getHexSeed());

        //generate the numbers

        attackerRolls = new int[attackerArmies];
        defenderRolls = new int[defenderArmies];

        for (int i = 0; i<attackerArmies; i++) {
            attackerRolls[i] = rng.getRandomByte();
        }

        for (int i = 0; i<defenderArmies; i++) {
            defenderRolls[i] = rng.getRandomByte();
        }

        gameState.getPlayer(attackerId).getClient().pushAction(buildAction(gameState));


    }

    public void setDefenderRolls(int[] rolls){
        defenderRolls = rolls;
    }
    public void setAttackerRolls(int[] rolls){
        attackerRolls = rolls;
    }


}
