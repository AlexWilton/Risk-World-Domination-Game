package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.Command;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.PingCommand;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.PlayersJoinedCommand;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.ReadyCommand;

import java.util.ArrayList;

public class MessageQueue {
    private final Integer ID;
    private boolean flag = false;
    private static Command command;
    private boolean[] player_connected;
    private boolean[] sentMessage;

    public MessageQueue(int players, boolean playing) {
        this.ID = playing? 0:null;
        sentMessage = new boolean[players];
        player_connected = new boolean[players];
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
        notifyAll();
        sentMessage[id] = true;
        if (sentAll()){
            flag = false;
        }
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
        sendAll(new PlayersJoinedCommand(players));
    }

    public synchronized void sendPing(int payload) {
        sendAll(new PingCommand(ID, payload));
    }

    public synchronized void sendReady() {
        sendAll(new ReadyCommand(ID, 1));
    }

    public synchronized void sendAll(Command command) {
        if (flag){
            try {
                wait();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        this.command = command;
        flag = true;
        for (int i = 0; i<sentMessage.length; i++)
            sentMessage[i] = false;
        notifyAll();
    }

    public synchronized void addPlayer(int id){
        player_connected[id] = true;
    }
}