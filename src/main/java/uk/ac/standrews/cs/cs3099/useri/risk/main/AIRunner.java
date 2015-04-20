package uk.ac.standrews.cs.cs3099.useri.risk.main;

import uk.ac.standrews.cs.cs3099.useri.risk.clients.Client;
import uk.ac.standrews.cs.cs3099.useri.risk.clients.WebClient;
import uk.ac.standrews.cs.cs3099.useri.risk.game.GameEngine;
import uk.ac.standrews.cs.cs3099.useri.risk.helpers.ClientSocketHandler;

public class AIRunner implements Runnable {
    private Client client;
    private String name;
    private String address;
    private int port;
    private WebClient webClient;
    private int result = -999;
    ClientSocketHandler socketHandler;

    public AIRunner(Client client, String name){
        this(client, name, "127.0.0.1", 8888, null);
    }

    public AIRunner(Client client, String name, String address, int port, WebClient webClient){
        this.client = client;
        this.name = name;
        this.address = address;
        this.port = port;
        this.webClient = webClient;
    }



    @Override
    public void run() {
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

    public int attemptConnecitonAndStartAi() {
        socketHandler = new ClientSocketHandler();
        socketHandler.setWebClientForWatchOnlyServer(webClient);
        float[] versions = {1};
        String[] features = {};

        //connect and obtain game information
        result = socketHandler.initialise(address,port,client,versions,features,name);

        Thread t = new Thread(this);
        t.start();

        return result;
    }
}
