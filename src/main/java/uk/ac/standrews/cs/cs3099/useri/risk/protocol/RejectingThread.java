package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.AcknowledgementCommand;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.JoinGameCommand;
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
public class RejectingThread implements Runnable {
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
                BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
                PrintWriter output = new PrintWriter(s.getOutputStream());
                if (JoinGameCommand.parse(input.readLine()) == null)
                    output.print(new AcknowledgementCommand(0));
                else
                    output.print(new RejectJoinGameCommand("Game already in progress"));

                input.close();
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
