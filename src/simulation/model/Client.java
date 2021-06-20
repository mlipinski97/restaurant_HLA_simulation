package simulation.model;

public class Client {
    private int id;

    public Client(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }


    double arrivalTime;
    double enterTheRestaurant;
    double leavingTime;

    public double getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public double getEnterTheRestaurant() {
        return enterTheRestaurant;
    }

    public void setEnterTheRestaurant(double enterTheRestaurant) {
        this.enterTheRestaurant = enterTheRestaurant;
    }

    public double getLeavingTime() {
        return leavingTime;
    }

    public void setLeavingTime(double leavingTime) {
        this.leavingTime = leavingTime;
    }

    @Override
    public boolean equals(Object obj) {
        Client clientToCheck;
        if(obj.getClass().equals(this.getClass())){
            clientToCheck = (Client)obj;
        } else{
            return false;
        }
        return clientToCheck.getId() == this.getId();
    }
}
