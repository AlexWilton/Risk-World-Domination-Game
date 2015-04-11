package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.Command;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.PingCommand;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.PlayersJoinedCommand;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.ReadyCommand;

import java.util.ArrayList;

public class MessageQueue {
    private boolean flag = false;
    private static Command command;
    private boolean[] player_connected;
    private boolean[] sentMessage;
    private final Integer ID;

    public MessageQueue(int players , boolean isHostPlaying) {
        sentMessage = new boolean[players];
        player_connected = new boolean[players];
        ID = isHostPlaying? 0 : null;
    }


    public synchronized Command getMessage(int id) {
        if (!flag) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (sentMessage[id])
            return null;

        System.out.println("Sending " + command.toJSONString());
        notifyAll();
        sentMessage[id] = true;
        if (sentAll()){
            flag = false;
        }
        return command;
    }

    public synchronized Command probablygetMessage(int id) {
        if (!flag) {
            return null;
        }
        if (sentMessage[id])
            return null;

        System.out.println("Sending " + command.toJSONString());
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
        sendAll(new PlayersJoinedCommand(players), ID);
    }

    public synchronized void sendPing(int payload) {
        sendAll(new PingCommand(ID, payload), ID);
    }

    public synchronized void sendReady() {
        sendAll(new ReadyCommand(ID, 1), ID);
    }

    public synchronized void sendAll(Command command, Integer id) {
        if (flag){
            try {
                System.out.println("blocking " + command + "\n while still having " + this.command);
                wait();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        this.command = command;
        System.out.println("Queued " + command + " by " + id);
        flag = true;
        for (int i = 0; i<sentMessage.length; i++)
            sentMessage[i] = false;
        if (id != null) sentMessage[id] = true;
        notifyAll();
    }

    public synchronized boolean probablySendAll(Command command, Integer id) {
        if (flag){
            return false;
        }
        this.command = command;
        flag = true;
        for (int i = 0; i<sentMessage.length; i++)
            sentMessage[i] = false;
        if (id != null) sentMessage[id] = true;
        notifyAll();
        return true;
    }

    public synchronized void addPlayer(int id){
        player_connected[id] = true;
    }
}
