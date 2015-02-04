package uk.ac.standrews.cs.cs3099.useri.risk.game;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;

import java.util.ArrayList;

public class GameEngineTest {
    GameEngine engine;

    @Before
    public void setUp() throws Exception {
        engine = new GameEngine();
        State state = new State();
        ArrayList<Client> clients = new ArrayList<Client>();
        engine.initialise(state);
    }

    @Test
    public void testGameLoop() throws Exception {
        //TODO need to test game loop!!!
       // engine.gameLoop();
    }
}