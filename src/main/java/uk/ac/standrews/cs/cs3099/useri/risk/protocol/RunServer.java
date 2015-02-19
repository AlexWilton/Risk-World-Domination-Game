package uk.ac.standrews.cs.cs3099.useri.risk.protocol;

/**
 * Created by bs44 on 19/02/15.
 */
public class RunServer {
    public static void main(String[] args) {
        ServerSocketHandler handler = new ServerSocketHandler(1234, 2, 30);
        handler.startServer(true);
    }
}
