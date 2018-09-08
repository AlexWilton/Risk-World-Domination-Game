package uk.ac.standrews.cs.cs3099.useri.risk.clients;

import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.webClient.JettyServer;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class WebServerClientTest {
    private JettyServer jettyServer;

    @Before
    public void setup(){
        jettyServer = new JettyServer(null);
    }

    @Test
    public void canServerStartAndStop(){
        jettyServer.run();
        assertTrue(jettyServer.isStarted());
        try {
            jettyServer.stop();
        } catch (Exception e) {
           fail();
        }
        assertTrue(jettyServer.isStopped());
    }
}
