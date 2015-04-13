package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.Command;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.PingCommand;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.PlayersJoinedCommand;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.ReadyCommand;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MessageQueue {
    private boolean flag = false;
    private static Command command;
    private boolean[] player_connected;
    private boolean[] sentMessage;
    private final Integer ID;
    private HashMap<Integer, PrintWriter> sockets = new HashMap<>();

    public MessageQueue(int players , boolean isHostPlaying) {
        sentMessage = new boolean[players];
        player_connected = new boolean[players];
        ID = isHostPlaying? 0 : null;
    }


    
    public synchronized Command probablyGetMessage(int id) {
        if (!flag) {
            return null;
        }
        if (sentMessage[id])
            return null;

        sentMessage[id] = true;
        if (sentAll()){
            flag = false;
        }
        notifyAll();
        return command;
    }

    private synchronized boolean sentAll() {
        for (int i = 0; i<sentMessage.length; i++){
            if (!sentMessage[i] && player_connected[i]){
                return false;
            }
        }
        return true;
    }

    public synchronized void sendPlayerList(ArrayList<Player> players){

        sendAll(new PlayersJoinedCommand(players), null);
    }

    public synchronized void sendPing(int payload) {
        sendAll(new PingCommand(ID, payload), ID);
    }

    public synchronized void sendReady() {
        sendAll(new ReadyCommand(ID, 1), ID);
    }

    public synchronized void sendAll(Command comm, Integer id) {
        for (Map.Entry<Integer, PrintWriter> e : sockets.entrySet()){
            if (e.getKey() != id){
                PrintWriter w = e.getValue();
                //System.out.println(e.getValue());
                System.out.println("Player " + e.getKey() + ": " + comm);
                w.println(comm);
                w.flush();
            }
        }
    }

    public synchronized void addPlayer(int id, PrintWriter out){
        player_connected[id] = true;
        sockets.put(id, out);
    }
}
