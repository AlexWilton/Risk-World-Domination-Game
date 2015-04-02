package uk.ac.standrews.cs.cs3099.useri.risk.protocol;


import uk.ac.standrews.cs.cs3099.useri.risk.clients.WebClient;

public class RunServer {
    public static void main(String[] args) {
        ServerSocketHandler handler = new ServerSocketHandler(1234, 2, new WebClient());
        handler.startServer(false);
    }
}
