package simulation.model;

import java.util.Optional;

public class DiningTable {
    private boolean isOccupied;
    private int id;
    private Optional<Client> currentClient;

    public DiningTable(int id) {
        this.id = id;
        currentClient = Optional.empty();
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void occupyTable(Client client) {
        this.currentClient = Optional.of(client);
        this.isOccupied = true;
    }

    public int getId() {
        return id;
    }

    public Optional<Client> getCurrentClient() {
        return currentClient;
    }

    public Client freeTable(){
        Client client = currentClient.get();
        currentClient = Optional.empty();
        isOccupied = false;
        return client;
    }


}
