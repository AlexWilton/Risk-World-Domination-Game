package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

import uk.ac.standrews.cs.cs3099.useri.risk.game.Player;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.PlayersJoined;

import java.util.ArrayList;

public class SignalJoinedPlayer {
    boolean flag = false;
    private ArrayList<ListenerThread> clientSocketPool;

    public SignalJoinedPlayer(ArrayList<ListenerThread> clientSocketPool) {
        this.clientSocketPool = clientSocketPool;
    }

    public synchronized PlayersJoined signal() {
        if (!flag) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Player[] arr = new Player[clientSocketPool.size()];
        int i = 0;
        for (ListenerThread l : clientSocketPool) {
            arr[i++] = l.client.getPlayer();
        }
        flag = false;
        notifyAll();
        return new PlayersJoined(arr);
    }

    public synchronized void send(){
        if (flag) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        flag = true;
        notify();
    }
}
