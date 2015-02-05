package uk.ac.standrews.cs.cs3099.useri.risk.clients;

import ec.util.MersenneTwisterFast;

import java.util.ArrayList;

/**
 * Created by po26 on 05/02/15.
 */
public class RiskDice {

    public static final int SEED_ARRAY_LENGTH = 8; //32 byte
    public static final int ATTACK_ROLL_FACES = 6;


    int faces;
    int count;

    ArrayList<int[]> seedComponents;

    public RiskDice(int faces, int count){
        this.faces = faces;
        this.count = count;
        this.seedComponents = new ArrayList<int[]>();
    }

    public void addSeedComponent(int[] seedComponent){

        if (seedComponent.length != SEED_ARRAY_LENGTH){
            //TODO handle error
        }
        else {
            this.seedComponents.add(seedComponent);
        }

    }

    public int[] getBattleDiceRolls(int from, int to){

        int ret[] = new int[to-from];

        int[] seed = calculateSeed();

        MersenneTwisterFast twister = new MersenneTwisterFast();
        twister.setSeed(seed);

        for (int i = 0; i < to; i++){
            if (i-from >= 0){
                ret[i-from] = twister.nextInt(faces - 1) + 1;
            }

        }

        return ret;


    }

    private int[] calculateSeed(){
        int[] ret = new int[SEED_ARRAY_LENGTH];

        for (int[] comp : seedComponents){

            for (int i = 0; i< SEED_ARRAY_LENGTH; i++){
                ret[i] ^= comp[i];
            }

        }

        return ret;

    }

}
