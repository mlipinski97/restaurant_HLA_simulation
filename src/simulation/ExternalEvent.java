package simulation;

import simulation.model.Client;

import java.util.Comparator;

public class ExternalEvent {

    public enum EventType {CLIENT_ARRIVAL, CLIENT_LEAVING_QUEUE, TABLE_BEING_FREED, NOTICE_ABOUT_AMOUNT_OF_TABLES,
        OCCUPY_TABLE,CLIENT_LEAVING_RESTAURANT,
        ASK_FOR_WAITER,END_OF_SERVICE,ASK_FOR_WAITER_AGAIN,
        END_SIM;
    }
    private Client client;
    private int parameter = 0;
    private Double time;
    private EventType eventType;
    String name;

    public String getName() {
        return name;
    }

    public ExternalEvent(EventType eventType, double time, int parameter) {
        this.parameter = parameter;
        this.time = time;
        this.eventType = eventType;
    }

    public ExternalEvent(Client client, EventType eventType, Double time) {
        this.client = client;
        this.time = time;
        this.eventType = eventType;
    }

    public ExternalEvent( String name,EventType eventType,Double time) {
        this.name = name;
        this.time = time;
        this.eventType = eventType;
    }

    public int getParameter() {
        return parameter;
    }

    public Client getClient() {
        return client;
    }

    public EventType getEventType() {
        return eventType;
    }

    public double getTime() {
        return time;
    }

    public static class ExternalEventComparator implements Comparator<ExternalEvent> {
        @Override
        public int compare(ExternalEvent o1, ExternalEvent o2) {
            return o1.time.compareTo(o2.time);
        }
    }
}
