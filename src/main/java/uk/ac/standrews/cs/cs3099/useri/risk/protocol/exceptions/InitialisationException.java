package uk.ac.standrews.cs.cs3099.useri.risk.protocol.exceptions;

/**
 * Thrown when the game initialisation fails in a thread.
 */
public class InitialisationException extends Throwable {
    /**
     * Constructor with String error message that will be sent to all other players with the leave_game command.
     * @param s error message
     */
    public InitialisationException(String s) {
        super(s);
    }
}
