package uk.ac.standrews.cs.cs3099.useri.risk.clients;

import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.Command;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.DefendCommand;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by patrick on 18/04/15.
 */
public class CommandQueuer {

    private Queue<Command> commandQueue;
    private Queue<String> hashQueue;
    private Queue<String> numberQueue;

    private DefendCommand defendCommand;

    public CommandQueuer(){
        commandQueue = new ArrayDeque<>();
        hashQueue = new ArrayDeque<>();
        numberQueue = new ArrayDeque<>();
    }

    public void pushRollHash(String rollHash){
        hashQueue.add(rollHash);
    }

    public void pushRollNumber(String rollNumber){
        numberQueue.add(rollNumber);
    }

    public String popRollHash(){
        try {
            while (hashQueue.size() < 1) Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return hashQueue.remove();
    }

    public String popRollNumber(){
        try {
            while (numberQueue.size() < 1) Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return numberQueue.remove();
    }

    public void pushCommand(Command command) {
        if (command instanceof DefendCommand){
            defendCommand=(DefendCommand)command;
        }
        else{
            commandQueue.add(command);
        }
    }


    public Command popCommand() {
        while (commandQueue.isEmpty()){

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return commandQueue.remove();
    }


    public DefendCommand popDefendCommand(int origin, int target, int armies) {
        while (defendCommand == null){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        DefendCommand ret = defendCommand;
        defendCommand = null;
        return ret;
    }
}
