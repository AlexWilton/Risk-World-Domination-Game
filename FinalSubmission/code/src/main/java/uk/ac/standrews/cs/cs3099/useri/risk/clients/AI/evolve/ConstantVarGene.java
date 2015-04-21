package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.genetic.Gene;

import java.util.Random;

/**
 * represents a constant var weight
 */
public class ConstantVarGene implements Gene{

    private static Random r = new Random();
    private static int MIN = -100;
    private static int MAX = 100;

    private int value;

    public ConstantVarGene(){
        value = Math.abs(r.nextInt())%(MAX-MIN) + MIN;
    }

    public ConstantVarGene(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public void mutate() {
        value = Math.abs(r.nextInt())%(MAX-MIN) + MIN;
    }

    @Override
    public Gene clone() {
        return new ConstantVarGene(value);
    }

    @Override
    public String toInformationString() {
        return ""+value;
    }

    @Override
    public void initFromString(String in) {
        value = Integer.parseInt(in);

    }
}
