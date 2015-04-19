package uk.ac.standrews.cs.cs3099.useri.risk.main;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.game.GameEngine;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.ClientSocketHandler;

public class AIRunner implements Runnable {
    private Client client;
    private String name;
    private String address;
    private int port;


    public AIRunner(Client client, String name){
        this(client, name, "127.0.0.1", 8888);
    }

    public AIRunner(Client client, String name, String address, int port){
        this.client = client;
        this.name = name;
        this.address = address;
        this.port = port;
    }


    @Override
    public void run() {
        ClientSocketHandler socketHandler = new ClientSocketHandler();
        float[] versions = {1};
        String[] features = {};

        //connect and obtain game information
        int result = socketHandler.initialise(address,port,client,versions,features,name);

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
