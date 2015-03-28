package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

/**
 * Created by bentlor on 28/03/15.
 */
public enum InitState {
    STAGE_CONNECTING,
    STAGE_PING,
    STAGE_READY,
    STAGE_PLAYING;

    private static InitState[] vals = values();

    /**
     * Gives the next stage. The FINISH stage would wrap to the first stage. Implementation seen at
     * http://stackoverflow.com/questions/17006239/whats-the-best-way-to-implement-next-and-previous-on-an-enum-type
     * @return
     */
    public InitState next() {
        return vals[ordinal()<vals.length-1? (ordinal() + 1): ordinal()];
    }

}
