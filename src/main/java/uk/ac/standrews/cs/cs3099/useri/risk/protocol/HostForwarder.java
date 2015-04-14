package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.RNGSeed;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.AcknowledgementCommand;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.Command;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.RollHashCommand;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.RollNumberCommand;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.exceptions.RollException;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * Class only for forwarding messages from all clients to this client and vice versa.
 */
public class HostForwarder {
    private static State state;
    private static RNGSeed seed;

    private MessageQueue messageQueue;
    private final int MOVE_TIMEOUT;
    private final int ACK_TIMEOUT;
    private final int ID;
    private BufferedReader input;
    private boolean ack_received;
    private boolean move_required;

    private double timer = System.currentTimeMillis();
    private double diff;
    private int last_ack = 0;

    public HostForwarder(MessageQueue q, int move_timeout, int ack_timeout, int id, BufferedReader input) {
        messageQueue = q;
        MOVE_TIMEOUT = move_timeout;
        ACK_TIMEOUT = ack_timeout;
        ID = id;
        this.input = input;
        diff = MOVE_TIMEOUT;
    }

    public static void setState(State state) {
        HostForwarder.state = state;
    }

    public static void setSeed(RNGSeed seed) {
        HostForwarder.seed = seed;
    }

    void getFirstPlayer() throws IOException {
        Command comm = Command.parseCommand(input.readLine());
        messageQueue.sendAll(comm, ID);
        if (!(comm instanceof RollHashCommand)) {
            throw new RollException();
        }
        RollHashCommand hash = (RollHashCommand) comm;
        String hashStr = hash.get("payload").toString();
        seed.addSeedComponentHash(hashStr, ID);

        while (!seed.hasAllHashes());

        comm = Command.parseCommand(input.readLine());
        messageQueue.sendAll(comm, ID);
        if (!(comm instanceof RollNumberCommand)) {
            throw new RollException();
        }
        RollNumberCommand roll = (RollNumberCommand) comm;
        String rollStr = roll.get("payload").toString();
        seed.addSeedComponent(rollStr, ID);

        seed = null;
    }

    void shuffleDeck() throws IOException {
        Command comm = Command.parseCommand(input.readLine());
        messageQueue.sendAll(comm, ID);
        if (!(comm instanceof RollHashCommand)) {
            throw new RollException();
        }
        RollHashCommand hash = (RollHashCommand) comm;
        String hashStr = hash.get("payload").toString();
        seed.addSeedComponentHash(hashStr, ID);

        while (!seed.hasAllHashes());

        comm = Command.parseCommand(input.readLine());
        messageQueue.sendAll(comm, ID);
        if (!(comm instanceof RollNumberCommand)) {
            throw new RollException();
        }
        RollNumberCommand roll = (RollNumberCommand) comm;
        String rollStr = roll.get("payload").toString();
        seed.addSeedComponent(rollStr, ID);
    }

    void playGame() throws IOException {
        while(true) {
            if (move_required && System.currentTimeMillis() > timer + diff) {
                throw new SocketTimeoutException();
            }
            if (!ack_received && System.currentTimeMillis() > timer + diff) {
                throw new SocketTimeoutException();
            }
            if (input.ready()) {
                Command reply = Command.parseCommand(input.readLine());
                System.out.println("in Player " + ID + ": " + reply);
                messageQueue.sendAll(reply, ID);
                checkAckCases(reply);
            }
        }
    }

    private void checkAckCases(Command comm) {
        if (comm instanceof AcknowledgementCommand) {
            if (((AcknowledgementCommand) comm).getAcknowledgementId() == last_ack) {
                ack_received = true;
                timer = System.currentTimeMillis();
                diff = MOVE_TIMEOUT;
            }
        } else {
            move_required = false;
            timer = System.currentTimeMillis();
        }
    }

    public void signalAck(int ack_id) {
        last_ack = ack_id;
        timer = System.currentTimeMillis();
        ack_received = false;
        diff = ACK_TIMEOUT;
    }

    public void signalMove() {
        move_required = true;
        timer = System.currentTimeMillis();
        diff = MOVE_TIMEOUT;
    }

    boolean hasSeed() {
        return seed != null;
    }
}
