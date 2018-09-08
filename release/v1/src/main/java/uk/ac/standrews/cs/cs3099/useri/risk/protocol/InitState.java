package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

/**
 * Game initialisation state enum.
 */
public enum InitState {
    STAGE_CONNECTING,
    STAGE_PING,
    STAGE_READY,
    STAGE_PLAYING,
    FIRST_PLAYER_ELECTABLE, DECK_SHUFFLED;

    private static InitState[] vals = values();

    /**
     * Gives the next stage. Playing.next keeps the stage at playing, forever.
     */
    public InitState next() {
        return vals[ordinal()<vals.length-1? (ordinal() + 1): ordinal()];
    }

}
