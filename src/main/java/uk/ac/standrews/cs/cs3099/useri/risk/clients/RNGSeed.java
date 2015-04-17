package uk.ac.standrews.cs.cs3099.useri.risk.clients;

import org.apache.commons.lang3.StringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Seed to store the random numbers given from all the clients to roll the dice via
 * the pseudo-random generator given in the protocol specification.
 */
public class RNGSeed {
    private String[] components;
    private String[] componentHashes;
    private boolean[] gotHash;
    private boolean[] gotNumber;

    public RNGSeed(int players){
        components = new String[players];
        componentHashes = new String[players];
        gotHash = new boolean[players];
        gotNumber = new boolean[players];
        for (int i = 0; i<players;i++){
            gotHash[i] = false;
            gotNumber[i] = false;
            componentHashes[i] = null;
            components[i] = null;
        }
    }


    /**
     * Adds the given hash to the list of hashes.
     * @param hash
     * @param player
     */
    public void addSeedComponentHash(String hash, int player){
        componentHashes[player] = hash;
        gotHash[player] = true;
    }

    /**
     * return false if th given hash cannot be obtained from the given component.
     */
    public boolean addSeedComponent(String component, int player){
        if (componentHashes[player].equals(hexHashFromHexNumber(component)) && !gotNumber[player]) {
            components[player] = component;
            gotNumber[player] = true;
            return true;
        }
        else{
            return false;
        }
    }

    public boolean hasAllHashes(){
        for (boolean has : gotHash){
            if (!has)
                return false;
        }
        return true;
    }

    public boolean hasAllNumbers(){
        for (boolean has : gotNumber){
            if (!has)
                return false;
        }
        return true;
    }

    /**
     * XORs two hexadecimal strings.
     * @param s1
     * @param s2
     * @return s1 XOR s2
     */
    private String hexXor(String s1, String s2){
        String ret = "";
        for (int i = 0; i<s1.length();i++){
            int first = Integer.parseInt(s1.substring(i,i+1),16);
            int second = Integer.parseInt(s2.substring(i,i+1),16);
            int xor = first ^ second;
            ret += Integer.toHexString(xor);
        }
        return ret;
    }

    /**
     * Gets the hex seed required for the random number generator by XORing all the
     * given hex strings.
     * @return
     */
    public String getHexSeed(){
        String ret = StringUtils.repeat("0",64);
        for (String value : components){
            ret = hexXor(ret,value);
        }
        return ret;
    }

    public boolean hasHash(int player){
        return gotHash[player];
    }

    public boolean hasNumber(int player){
        return gotNumber[player];
    }

    /**
     * Converts an array of bytes to hexadecimal string.
     * @param value
     * @return
     */
    public static String toHexString(byte[] value){
        String ret = "";
        for (byte i : value){
            ret += StringUtils.leftPad(Integer.toHexString(i & 0xFF), 2, "0");
        }
        return ret;
    }

    /**
     * Converts string in hexadecimal to byte array.
     * @param s
     * @return
     */
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    /**
     * Calculates the SHA-256 hash of the given numbers
     * @param numbers given as byte array.
     * @return hashed numbers
     */
    private static byte[] calcHash(byte[] numbers){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(numbers);
            return digest.digest();
        }
        catch (NoSuchAlgorithmException e){
            System.out.println("Need sha256 algorithm");
            System.exit(0);
        }
        return null;
    }

    /**
     * Returns the SHA-256 hash of this number. Both shall be in hexadecimal.
     * @param hexNumber
     * @return
     */
    public static String hexHashFromHexNumber(String hexNumber){
        return toHexString(calcHash(hexStringToByteArray(hexNumber)));
    }

    /**
     * Generates a random 256-bit number and reurns it as a byte array.
     * @return
     */
    public static byte[] makeRandom256BitNumber(){
        Random r = new Random(System.currentTimeMillis());
        byte ret[] = new byte[32];
        r.nextBytes(ret);
        return ret;

    }



}