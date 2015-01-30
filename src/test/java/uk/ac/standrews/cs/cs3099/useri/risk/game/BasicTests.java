package uk.ac.standrews.cs.cs3099.useri.risk.game;

import junit.framework.TestCase;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;

import java.util.ArrayList;

public class BasicTests extends TestCase {
    GameEngine engine;

    public BasicTests(String name) {
        super(name);
    }


    public void setUp() throws Exception {
        engine = new GameEngine();
        State state = new State();
        ArrayList<Client> clients = new ArrayList<Client>();
        engine.initialise(state, clients);
    }

    public void test() throws Exception {
        engine.gameLoop();
    }


}