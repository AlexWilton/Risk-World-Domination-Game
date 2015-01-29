package uk.ac.standrews.cs.cs3099.useri.risk.game;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;

import java.util.ArrayList;

public class BasicTests extends TestCase {
    GameEngine engine;

    public BasicTests(String name) {
        super(name);
    }

    @Before
    public void setUp() throws Exception {
        engine = new GameEngine();
        State state = new State();
        ArrayList<Client> clients = new ArrayList<Client>();
        engine.initialise(state, clients);
    }

    @Test
    public void test() throws Exception {
        engine.gameLoop();
    }


}