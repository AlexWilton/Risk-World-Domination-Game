package uk.ac.standrews.cs.cs3099.risk.game;

/**
 * RandomNumbers Class
 * Generates pseudo random numbers using the RC4 cipher as a keyed RNG
 * If you don't like RC4/if its horribly broken and should never be used/if RC4
 * causes you mental distress, then cry harder. It's secure ;-)
 */
public class RandomNumbers {

    private char[] state = new char[256];
    private int x;
    private int y;


    /**
     * Seeds the "random" number generator using a variable length seed
     * @param	seed	the seed to use (in byte array)
     */
    public void initRng(byte[] seed)
    {
        char i, j, tmp; // Using chars because bytes are signed in java
        int len = seed.length;

        // Initialise the internal state
        x = 0;
        y = 0;

        for (i = 0; i < 256; i++)
            state[i] = i;

        // Permute using the key
        for (i = j = 0; i < 256; i++) {
            j = (char)((j + state[i] + (seed[i % len] & 0xFF)) % 256);

            // Swap indices i and j
            tmp = state[i];
            state[i] = state[j];
            state[j] = tmp;
        }

        // Generate 1024 bytes of output to avoid initial keystream bias
        for (i = 0; i < 1024; i++)
            getRandomByte();
    }

    public RandomNumbers(byte[] seed)
    {
        initRng(seed);
    }

    private byte[] hexToByteArray(String hex)
    {
        int len = hex.length();
        byte data[] = new byte[len >> 1];

        for (int i = 0; i < len; i += 2)
            data[i >> 1] = (byte)((Character.digit(hex.charAt(i), 16) << 4) +
                    Character.digit(hex.charAt(i + 1), 16));

        return data;
    }

    /**
     * Seeds the "random" number generator using a variable length seed
     * @param	hexseed	the seed to use (string in hex)
     */
    public RandomNumbers(String hexseed)
    {
        initRng(hexToByteArray(hexseed));
    }

    /**
     * Generates a random byte of output from the internal state
     * @return	a random byte
     */
    public byte getRandomByte()
    {
        char tmp;

        // Update indices
        x = (x + 1) % 256;
        y = (y + state[x]) % 256;

        // Swap indices i and j
        tmp = state[x];
        state[x] = state[y];
        state[y] = tmp;

        // Return the next byte
        return (byte)state[(state[x] + state[y]) % 256];
    }

    /**
     * Generates a random integer, using getRandomByte.
     * from most significant byte to least significant
     *
     * @return 	a random integer
     */
    public int getRandomInt()
    {
        int ret = 0;

        // I'm trying to make very clear the order in which to do this
        for (int i = 0; i < 4; i++) {
            ret <<= 8;
            ret |= (int)getRandomByte() & 0xFF; // I have to due this because of javas signedness
        }

        return ret;
    }


}
