package uk.ac.standrews.cs.cs3099.useri.risk.clients.webClient;

public class ServerTester {

    public static void main(String[] args) {
        JettyServer jettyServer = new JettyServer();
        System.out.println("Server starting...");
        jettyServer.run();
    }
}
