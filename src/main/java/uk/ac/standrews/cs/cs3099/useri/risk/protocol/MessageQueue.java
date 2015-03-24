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
    private int sent = 0;
    private int players;

    public MessageQueue(int players, boolean playing) {
        this.players = players;
        this.ID = playing? 0:null;
    }


    public synchronized Command getMessage(int players) {
        if (!flag) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        sent++;
        if (sent == players) {
            flag = false;
            sent = 0;
        }
        System.out.println("Sending " + command.toJSONString());
        notifyAll();
        return command;
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
        notifyAll();
    }
}
