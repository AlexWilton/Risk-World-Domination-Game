package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.RejectJoinGameCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Thread rejecting new connections after the game has already started. It really doesn't do anything else.
 */
class RejectingThread implements Runnable {
    private ServerSocket server;
    private boolean running = true;

    public RejectingThread(ServerSocket server) {
        this.server = server;
    }

    @Override
    public void run() {
        try {
            while (running) {
                Socket s = server.accept();
                PrintWriter output = new PrintWriter(s.getOutputStream());
                output.print(new RejectJoinGameCommand("Game already in progress"));
                output.flush();
                output.close();
                s.close();
            }
        } catch (IOException e) {
            // We don't care about exceptions here.
        }
    }

    public void stop() {
        running = false;
    }
}
