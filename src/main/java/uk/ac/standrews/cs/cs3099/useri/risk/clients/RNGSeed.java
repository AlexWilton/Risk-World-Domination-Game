package uk.ac.standrews.cs.cs3099.useri.risk.clients;

import org.apache.commons.lang3.StringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by patrick on 05/04/15.
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


    public void addSeedComponentHash(String hash, int player){
        componentHashes[player] = hash;
        gotHash[player] = true;
    }

    /**
     * return false if its wrong
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


    public static String toHexString(byte[] value){
        String ret = "";
        for (byte i : value){
            ret += StringUtils.leftPad(Integer.toHexString(i & 0xFF), 2, "0");
        }

        return ret;
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

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

    public static String hexHashFromHexNumber(String hexNumber){
        return toHexString(calcHash(hexStringToByteArray(hexNumber)));
    }

    public static byte[] makeRandom256BitNumber(){
        Random r = new Random(System.currentTimeMillis());
        byte ret[] = new byte[32];
        r.nextBytes(ret);
        return ret;

    }



}