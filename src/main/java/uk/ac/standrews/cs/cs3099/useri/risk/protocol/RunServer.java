package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

public class RunServer {
    public static void main(String[] args) {
        ServerSocketHandler handler = new ServerSocketHandler(1234, 2, 30);
        handler.startServer(true);
    }
}
