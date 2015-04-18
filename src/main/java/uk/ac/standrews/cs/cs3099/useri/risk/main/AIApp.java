package uk.ac.standrews.cs.cs3099.useri.risk.main;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.AI.RandomAIClient;
import uk.ac.standrews.cs.cs3099.useri.risk.game.GameEngine;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.ClientSocketHandler;

/**
 * Created by patrick on 17/04/15.
 */
public class AIApp {


    public static void main(String argv[]){
        ClientSocketHandler socketHandler = new ClientSocketHandler();
        float[] versions = {1};
        String[] features = {};

        //connect and obtain game information
        int result = socketHandler.initialise("127.0.0.1",8888,new RandomAIClient(null),versions,features,"bull");

        if(result == ClientApp.SUCCESS) {
            //Run the socket handler
            Thread socketHandlerThread = new Thread(socketHandler);
            socketHandlerThread.start();

            //initialise and start the game engine thread
            GameEngine engine = new GameEngine(socketHandler);
            Thread gameEngineThread = new Thread(engine);
            gameEngineThread.start();
        }


    }
}
