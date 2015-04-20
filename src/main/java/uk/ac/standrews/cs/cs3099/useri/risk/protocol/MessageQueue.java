package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.randomnumbers.RandomNumberGenerator;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.Command;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.PingCommand;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.PlayersJoinedCommand;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.ReadyCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Message Queue to distribute message to all the nodes connected
 */
class MessageQueue {
    private final Integer ID;
    private HashMap<Integer, ListenerThread> sockets = new HashMap<>();

    /**
     * Constructor to create message queue
     * @param isHostPlaying boolean variable to tell whether the host is playing or not
     */
    public MessageQueue(boolean isHostPlaying) {
        ID = isHostPlaying? 0 : null;
    }

    /**
     * Synchronised method to send ping message to all
     * @param payload
     * @throws IOException
     */
    public synchronized void sendPing(int payload) throws IOException {
        sendAll(new PingCommand(ID, payload), ID);
    }

    /**
     * Synchronised method to send everyone ready message
     * @throws IOException
     */
    public synchronized void sendReady() throws IOException {
        sendAll(new ReadyCommand(ID), ID);
    }

    /**
     * synchronised method to send message to all
     * @param comm Command to be sent
     * @param id
     * @throws IOException
     */
    public synchronized void sendAll(Command comm, Integer id)  throws IOException {
        System.out.println(comm);
        boolean signal_ack = comm.requiresAcknowledgement();
        int ack_id = -1;
        if (signal_ack)
            ack_id = Integer.parseInt(comm.get("ack_id").toString());

        for (Map.Entry<Integer, ListenerThread> e : sockets.entrySet()){
            if (e.getKey() != id){
                ListenerThread w = e.getValue();
                w.reply(comm);
                if (signal_ack){
                    w.signalAck(ack_id);
                }
            }
        }
    }

    /**
     * Notifying all the connected nodes that the player has been added
     * @param id integer ID
     * @param t Thread t
     * @param player Player object
     * @throws IOException
     */
    public synchronized void addPlayer(int id, ListenerThread t, Player player) throws IOException {
        sockets.put(id, t);
        ArrayList<Player> players = new ArrayList<>();
        players.add(player);
        sendAll(new PlayersJoinedCommand(players), id);
    }

    /**
     * Getting Dice rolls
     * @param id
     */
    public synchronized void getRolls(int id){
        HostForwarder.setSeed(new RandomNumberGenerator());
        try {
            for (Map.Entry<Integer, ListenerThread> e : sockets.entrySet()){
                if (e.getKey() != id){
                    ListenerThread w = e.getValue();
                    w.getRollsLater();
                }
            }
        } catch(IOException | InterruptedException e) {
            //There is nothing to do here, just exit, the protocol has failed.
            e.printStackTrace();
            System.err.println("Could not get dice rolls, protocol failed");
            System.exit(1);
        }
    }

    /**
     * Remove player
     * @param id
     */
    public synchronized void removePlayer(int id) {
        ListenerThread w = sockets.get(id);
        w.removePlayer();
    }
}
