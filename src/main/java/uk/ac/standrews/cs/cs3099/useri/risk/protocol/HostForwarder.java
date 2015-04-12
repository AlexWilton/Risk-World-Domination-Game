package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.game.State;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.AcknowledgementCommand;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * Created by bs44 on 12/04/15.
 */
public class HostForwarder  {
    private MessageQueue messageQueue;
    private final int MOVE_TIMEOUT;
    private final int ACK_TIMEOUT;
    private final int ID;
    private BufferedReader input;
    private PrintWriter output;
    private ArrayList<Player> players = new ArrayList<>();
    private boolean[] ack_received;

    private double timer = System.currentTimeMillis();
    private double diff;
    private int last_ack = 0;

    public HostForwarder(MessageQueue q, int move_timeout, int ack_timeout, int id, BufferedReader input, PrintWriter output){
        messageQueue = q;

        MOVE_TIMEOUT = move_timeout;
        ACK_TIMEOUT = ack_timeout;
        ID = id;
        this.input = input;
        this.output = output;

        diff = MOVE_TIMEOUT;
        players = ListenerThread.getPlayers();
        ack_received = new boolean[players.size()];
    }

    void playGame(State state) throws IOException {
        Command comm;
        while(true) {
            if (System.currentTimeMillis() > timer + MOVE_TIMEOUT)
                throw new SocketTimeoutException();
            if (!acks_received() && System.currentTimeMillis() > timer + diff) {
                throw new SocketTimeoutException();
            }
            while ((comm = messageQueue.probablygetMessage(ID)) != null) {
                reply(comm);
                checkAckCases(comm);
            }
            if (input.ready()) {
                comm = Command.parseCommand(input.readLine());
                messageQueue.sendAll(comm, ID);
                performOnState(comm, state);
                //System.out.println("Player " + ID + " received " + reply);
                checkAckCases(comm);
            }
        }
    }

    private void performOnState(Command comm, State state) {
        if (comm instanceof AcknowledgementCommand) {
            return;
        }
    }

    private void checkAckCases(Command comm) {
        if (comm.requiresAcknowledgement()){
            timer = System.currentTimeMillis();
            ack_required(comm.getPlayer());
            diff = ACK_TIMEOUT;
            last_ack = Integer.parseInt(comm.get("ack_id").toString());

        } else if (comm instanceof AcknowledgementCommand) {
            if (((AcknowledgementCommand) comm).getAcknowledgementId() == last_ack) {
                ack_received[getNumber(comm.getPlayer())] = true;
                if (acks_received()) {
                    timer = System.currentTimeMillis();
                    diff = MOVE_TIMEOUT;
                }
            }
        } else if (acks_received()) {
            timer = System.currentTimeMillis();
        } else {
            diff = MOVE_TIMEOUT;
        }
    }

    private void ack_required(int player) {
        for (int i = 0; i< ack_received.length; i++){
            ack_received[i] = false;
        }
        Integer j;
        if ((j = getNumber(player)) != null) {
            ack_received[j] = true;
        }
    }

    private boolean acks_received() {
        for (boolean b : ack_received) {
            if (!b)
                return false;
        }
        return true;
    }

    private Integer getNumber(int player) {
        for (Player p: players){
            if (p.getID() == player)
                return players.indexOf(p);
        }
        return null;
    }

    private void reply(Command command) {
        if (command == null)
            return;
        System.out.println("Player " + ID + ": " + command);
        output.println(command);
        output.flush();
    }
}
