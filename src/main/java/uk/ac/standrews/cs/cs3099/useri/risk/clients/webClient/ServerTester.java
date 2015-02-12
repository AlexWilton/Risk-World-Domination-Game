package uk.ac.standrews.cs.cs3099.useri.risk.clients.webClient;

public class ServerTester {

    public static void main(String[] args) {
        JettyServer jettyServer = new JettyServer();
        jettyServer.run();
        while (true) {
            try {
                Thread.sleep(5000);                 //1000 milliseconds is one second.
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            System.out.println("Server stopping...");
            try {
                jettyServer.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(5000);                 //1000 milliseconds is one second.
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            System.out.println("Server starting...");
            jettyServer.run();
        }
    }
}
