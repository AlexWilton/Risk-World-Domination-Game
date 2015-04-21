package uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.evolve.genetic.Gene;

import java.util.Random;


public class MultiplierGene implements Gene{
    private static Random r = new Random();

    private double value;

    public MultiplierGene(){
        value = r.nextDouble()*2.0f-1.0f;
    }

    public MultiplierGene(double value){
        this.value = value;
    }

    @Override
    public void mutate() {
        value = r.nextDouble()*2.0f-1.0f;
    }

    @Override
    public Gene clone() {
        return new MultiplierGene(value);
    }

    @Override
    public String toInformationString() {
        return ""+value;
    }

    @Override
    public void initFromString(String in) {
        value = Double.parseDouble(in);

    }

    public double getValue() {
        return value;
    }
}
