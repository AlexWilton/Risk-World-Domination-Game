package uk.ac.standrews.cs.cs3099.useri.risk.helpers;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.RNGSeed;

import java.util.Arrays;

/**
 * Created by patrick on 08/04/15.
 */
class MakeRandomAndHashTuples {
    public static void main (String argv[]){
        for (int i = 0; i<10;i++){
            byte[] random = RNGSeed.makeRandom256BitNumber();
            System.out.println("\"" + RNGSeed.toHexString(random) + "\",\"" + RNGSeed.hexHashFromHexNumber(RNGSeed.toHexString(random)) + "\"");
        }

    }
}
