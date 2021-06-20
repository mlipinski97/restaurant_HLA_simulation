package simulation.statistics;

import hla.rti.RTIexception;
import simulation.AbstractFederate;
import simulation.ExternalEvent;
import simulation.Settings;
import simulation.model.Client;
import simulation.waiter.WaiterFederate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StatisticsFederate extends AbstractFederate {

    public StatisticsFederate() {
        super("StatisticsFederate", new StatisticsAmbasador());
    }

    List<Client> clients = new ArrayList<>();
    int endSimulatiuon = 0;
    double stopNewClients = 0;
    double stopedSym=0;
    @Override
    protected void federateCoreWork() throws RTIexception {

        while (fedamb.running) {

            double timeToAdvance = fedamb.federateTime + timeStep;
            advanceTime(timeToAdvance);


            if (fedamb.externalEvents.size() > 0) {
                fedamb.externalEvents.sort(new ExternalEvent.ExternalEventComparator());
                Client client;
                for(ExternalEvent externalEvent : fedamb.externalEvents ){
                    //log(externalEvent.getTime()+ ":: " + externalEvent.getEventType());
                    switch (externalEvent.getEventType()) {
                        case OCCUPY_TABLE:
                            client = clients.stream().filter(e->e.getId()==externalEvent.getClient().getId()).findAny().get();
                            client.setEnterTheRestaurant(externalEvent.getTime());
                            break;
                        case CLIENT_ARRIVAL:
                            client = externalEvent.getClient();
                            client.setArrivalTime(externalEvent.getTime());
                            clients.add(client);
                            break;
                        case CLIENT_LEAVING_RESTAURANT:
                            client = clients.stream().filter(e->e.getId()==externalEvent.getClient().getId()).findAny().get();
                            client.setLeavingTime(externalEvent.getTime());
                            break;
                        case END_SIM:
                            String federateName = externalEvent.getName();
                            if(federateName.equals("ClientFederate"))
                                stopNewClients=externalEvent.getTime();
                            endSimulatiuon++;
                            if(endSimulatiuon==4) {
                                stopedSym = externalEvent.getTime();
                                fedamb.running = false;
                            }
                            break;
                    }
                }
            }

            if (fedamb.grantedTime == timeToAdvance) {
                timeToAdvance += fedamb.federateLookahead;
                fedamb.federateTime = timeToAdvance;
            }
            rtiamb.tick();
        }

        double avg = clients.stream().filter(e->e.getEnterTheRestaurant()!=0).mapToDouble(e -> e.getEnterTheRestaurant()-e.getArrivalTime()).average().getAsDouble();
        double avgRestaurantTime = clients.stream().filter(e->e.getLeavingTime()!=0).mapToDouble(e -> e.getLeavingTime()-e.getEnterTheRestaurant()).average().getAsDouble();
        double avgAllTime = clients.stream().filter(e->e.getLeavingTime()!=0).mapToDouble(e -> e.getLeavingTime()-e.getArrivalTime()).average().getAsDouble();

        log("Sredni okres czekania w kolejce to: " + avg);
        log("Sredni czas przebywania w restauracji to: " + avgRestaurantTime);
        log("Sredni czas przebywania w symualcji to: " + avgAllTime);
        log("Restauracja zaczała się zamykać o: " +stopNewClients);
        log("Symulacja trwała: "+stopedSym);
        log("Nadgodziny trwały: " +(stopedSym- stopNewClients));
    }

    @Override
    protected void publishAndSubscribeCore() throws RTIexception {
        /////////////////////////subscribe////////////////////////////////
        int clientEnteringRestaurantHandle = rtiamb.getInteractionClassHandle("InteractionRoot.ClientEnteringRestaurant");
        fedamb.handlerMap.put("InteractionRoot.ClientEnteringRestaurant", clientEnteringRestaurantHandle);
        rtiamb.subscribeInteractionClass(clientEnteringRestaurantHandle);

        int clientArrivalHandle = rtiamb.getInteractionClassHandle("InteractionRoot.ClientArrival");
        fedamb.handlerMap.put("InteractionRoot.ClientArrival", clientArrivalHandle);
        rtiamb.subscribeInteractionClass(clientArrivalHandle);

        int clientLeaving = rtiamb.getInteractionClassHandle("InteractionRoot.ClientLeavingRestaurant");
        fedamb.handlerMap.put("InteractionRoot.ClientLeavingRestaurant", clientLeaving);
        rtiamb.subscribeInteractionClass(clientLeaving);
    }

    public static void main(String[] args) {
        try {
            new StatisticsFederate().runFederate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
