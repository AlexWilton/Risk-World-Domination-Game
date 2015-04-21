package uk.ac.standrews.cs.cs3099.useri.risk.main;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.webClient.WebClient;

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
    }

}
