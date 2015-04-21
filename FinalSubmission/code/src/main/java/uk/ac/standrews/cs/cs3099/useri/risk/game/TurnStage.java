package uk.ac.standrews.cs.cs3099.useri.risk.game;

/**
 * Created by bs44 on 01/02/15.
 * This is an enum type that would identify each stage of one player's turn in a game. These each correspond to one
 * action that the player can make, however, there might be actions (like attacking), that can be repeated indefinitely.
 */
public enum TurnStage {
    STAGE_TRADING,
    STAGE_DEPLOYING,
    STAGE_BATTLES,
    STAGE_GET_CARD,
    STAGE_FORTIFY,
    STAGE_FINISH;

    private static TurnStage[] vals = values();

    /**
     * Gives the next stage. The FINISH stage would wrap to the first stage. Implementation seen at
     * http://stackoverflow.com/questions/17006239/whats-the-best-way-to-implement-next-and-previous-on-an-enum-type
     * @return next stage.
     */
    public TurnStage next() {
        return vals[(ordinal() + 1) % vals.length];
    }


}
