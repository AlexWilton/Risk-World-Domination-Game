package uk.ac.standrews.cs.cs3099.useri.risk.clients.webClient;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.WebClient;
import uk.ac.standrews.cs.cs3099.useri.risk.game.GameEngine;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.TestGameStateFactory;

public class ServerTester {

    public static void main(String[] args) {

        WebClient webClient = new WebClient();
        webClient.setState(TestGameStateFactory.getWebClientTestState(webClient));
        GameEngine gameEngine = new GameEngine();
        gameEngine.initialise(webClient.getState());
        gameEngine.gameLoop();
    }
}
