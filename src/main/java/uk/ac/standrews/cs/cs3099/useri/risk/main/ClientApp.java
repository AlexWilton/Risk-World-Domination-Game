package uk.ac.standrews.cs.cs3099.useri.risk.main;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.game.GameEngine;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.ClientSocketHandler;

/**
 * handles threads for clients
 */
public class ClientApp {


    public static final int SUCCESS = 0;
    public static final int BAD_ADDRESS = -1;
    public static final int CANNOT_OPEN_STREAMS = -2;
    public static final int JOIN_REJECTED = -3;
    public static final int COMMUNICATION_FAILED = -4;
    public static final int PROTOCOL_ERROR_DETECTED = -5;


    /**
     * Run Client. Attempt make connection to server. Start local game engine for client.
     * @param address of the server
     * @param port of the connecting server
     * @param localClient Client instance
     * @param playerName Name of Player
     * @return state of initialisation of the game
     */
    public static int run (String address, int port, Client localClient, String playerName) {
        if(playerName == null || playerName.equals("")) playerName = "(Nameless)";
        ClientSocketHandler socketHandler = new ClientSocketHandler();
        float[] versions = {1};
        String[] features = {};

        //connect and obtain game information
        int result = socketHandler.initialise(address,port,localClient,versions,features,playerName);

        if(result == SUCCESS) {
            //Run the socket handler
            Thread socketHandlerThread = new Thread(socketHandler);
            socketHandlerThread.start();

            //initialise and start the game engine thread
            GameEngine engine = new GameEngine(socketHandler);
            Thread gameEngineThread = new Thread(engine);
            gameEngineThread.start();
        }

        return result;
       
    }

}
