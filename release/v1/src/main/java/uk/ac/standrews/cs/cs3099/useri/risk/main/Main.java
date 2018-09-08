package uk.ac.standrews.cs.cs3099.useri.risk.main;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.webClient.WebClient;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Main Class for start of program execution
 */
public class Main {

    /**
     * Launch Web Client (opens in browser), which provides options to
     * host a game and connect to other games.
     * @param args
     */
    public static void main(String[] args){
        new WebClient();
        try {
            PrintStream output = new PrintStream(new FileOutputStream("log.txt"));
            System.setOut(output);
            System.setErr(output);
        } catch (FileNotFoundException e) {
            System.err.println("log.txt could not be opened!");
        }

    }

}
