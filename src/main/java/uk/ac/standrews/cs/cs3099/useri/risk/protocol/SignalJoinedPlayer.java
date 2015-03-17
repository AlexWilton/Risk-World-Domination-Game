package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.Command;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.PingCommand;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.PlayersJoined;

import java.util.ArrayList;

public class SignalJoinedPlayer {
    boolean flag = false;
    private static Command command;
    private int sent = 0;
    private int players;

    public SignalJoinedPlayer(int players) {
        this.players = players;
    }


    public synchronized Command signal(int players) {
        if (!flag) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        sent++;
        if (players == sent || sent == this.players) {
            flag = false;
            sent = 0;
        }
        System.out.println("Sending...");
        notifyAll();
        return command;
    }

    public synchronized void send(ArrayList<Player> players){
        if (flag) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Creating message");
        command = new PlayersJoined(players);
        flag = true;
        notifyAll();
    }

    public synchronized void sendGameStarted(Integer ID, int payload) {
        if (flag) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Creating message");
        command = new PingCommand(ID, payload);
        flag = true;
        notifyAll();
    }
}
