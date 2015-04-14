package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.Command;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.PingCommand;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.PlayersJoinedCommand;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.ReadyCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MessageQueue {
    private final Integer ID;
    private HashMap<Integer, ListenerThread> sockets = new HashMap<>();

    public MessageQueue(boolean isHostPlaying) {
        ID = isHostPlaying? 0 : null;
    }

    public synchronized void sendPing(int payload) throws IOException {
        sendAll(new PingCommand(ID, payload), ID);
    }

    public synchronized void sendReady() throws IOException {
        sendAll(new ReadyCommand(ID), ID);
    }

    public synchronized void sendAll(Command comm, Integer id)  throws IOException {
        boolean signal_ack = comm.requiresAcknowledgement();
        int ack_id = -1;
        if (signal_ack)
            ack_id = Integer.parseInt(comm.get("ack_id").toString());
        for (Map.Entry<Integer, ListenerThread> e : sockets.entrySet()){
            if (e.getKey() != id){
                ListenerThread w = e.getValue();
                w.reply(comm);
                if (signal_ack){
                    w.signal(ack_id);
                }
            }
        }
    }

    public synchronized void addPlayer(int id, ListenerThread t, Player player) throws IOException {
        sockets.put(id, t);
        ArrayList<Player> players = new ArrayList<>();
        players.add(player);
        sendAll(new PlayersJoinedCommand(players), id);
    }
}
