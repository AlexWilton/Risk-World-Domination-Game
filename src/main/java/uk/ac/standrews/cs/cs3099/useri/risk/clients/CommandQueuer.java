package uk.ac.standrews.cs.cs3099.useri.risk.clients;

import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.Command;
import uk.ac.standrews.cs.cs3099.useri.risk.protocol.commands.DefendCommand;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Ques and field to hold commands for different types
 */
public class CommandQueuer {

    private Queue<Command> commandQueue;
    private Queue<String> hashQueue;
    private Queue<String> numberQueue;

    private DefendCommand defendCommand;

    /**
     * Constructor of CommandQueuer, initialises each queues
     */
    public CommandQueuer(){
        commandQueue = new ArrayDeque<>();
        hashQueue = new ArrayDeque<>();
        numberQueue = new ArrayDeque<>();
    }

    /**
     * Adds RollHash of the Client
     * @param rollHash
     */
    public void pushRollHash(String rollHash){
        hashQueue.add(rollHash);
    }

    /**
     * Adds roll number
     * @param rollNumber
     */
    public void pushRollNumber(String rollNumber){
        numberQueue.add(rollNumber);
    }

    /**
     * Adds command to appropriate command queues
     * @param command
     */
    public void pushCommand(Command command) {
        if (command instanceof DefendCommand){
            defendCommand=(DefendCommand)command;
        }
        else{
            commandQueue.add(command);
        }
    }

    /**
     * Returns the RollHash and removes it from the queue
     * @return String rollhash
     */
    public String popRollHash(){
        try {
            while (hashQueue.size() < 1) Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return hashQueue.remove();
    }

    /**
     * Returns and removes roll number from the queue
     * @return String Rollnumber
     */
    public String popRollNumber(){
        try {
            while (numberQueue.size() < 1) Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return numberQueue.remove();
    }

    /**
     * Return and remove Command from the commandqueue
     * Also waits for commands to be added by another thread. Busy-wait impl.
     * @return Command which was in the queue
     */
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

    /**
     * Returns defend command, also has an mechanism to wait for command to be stored
     *
     * @param origin int id of the origin country
     * @param target int id of the target country
     * @param armies int numbers of the armies to be used to defend
     * @return DefendCommand that has been stored
     */
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