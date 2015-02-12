package uk.ac.standrews.cs.cs3099.useri.risk.game;

import uk.ac.standrews.cs.cs3099.useri.risk.helpers.ClientSocketDistributor;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.TestGameStateFactory;

/**
 * Hello world!!!
 */
public class App {
    public static void main(String[] args) {

       // System.out.println("Hello World");

        State gameState = TestGameStateFactory.getTestGameState();

        GameEngine gameEngine = new GameEngine();
        gameEngine.initialise(gameState);
        gameEngine.gameLoop();

        /*ClientSocketDistributor d = new ClientSocketDistributor("localhost",1234,null);
        new Thread(d).start();*/

    }
}
