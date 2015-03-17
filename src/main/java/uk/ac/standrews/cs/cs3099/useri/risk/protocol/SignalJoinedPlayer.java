package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.Command;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.PlayersJoined;

import java.util.ArrayList;

public class SignalJoinedPlayer {
    boolean flag = false;
    private ArrayList<ListenerThread> clientSocketPool;
    private static Command command;

    public SignalJoinedPlayer(ArrayList<ListenerThread> clientSocketPool) {
        this.clientSocketPool = clientSocketPool;
    }

    public synchronized Command signal() {
        if (!flag) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        flag = false;
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
}
