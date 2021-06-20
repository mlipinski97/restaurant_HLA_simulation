package simulation.model;

import java.util.ArrayDeque;
import java.util.Queue;

public class WaitingQueue {
    private Queue<Client> waitingClients;

    public WaitingQueue() {
        this.waitingClients = new ArrayDeque<>();
    }

    public void addClientToQueue(Client client){
        waitingClients.add(client);
    }

    public int getQueueSize(){
        return waitingClients.size();
    }

    public Client removeFirstClientFromQueue(){
        return waitingClients.poll();
    }

    public void removeChosenClientFromQueue(Client client){
        waitingClients.remove(client);
    }

}
