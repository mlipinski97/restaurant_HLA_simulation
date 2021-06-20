package simulation.restaurant;

import hla.rti.LogicalTime;
import hla.rti.RTIexception;
import hla.rti.SuppliedParameters;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import simulation.AbstractFederate;
import simulation.ExternalEvent;
import simulation.Settings;
import simulation.model.Client;
import simulation.model.DiningTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;

//Projekt stworzony na podstawie ISO

public class RestaurantFederate extends AbstractFederate {
    public static final String READY_TO_RUN = "ReadyToRun";

    private List<DiningTable> tables;
    SplittableRandom random = new SplittableRandom();

    public RestaurantFederate() {
        super("RestaurantFederate", new RestaurantAmbassador());
        tables = new ArrayList<>();
        for (int i = 1; i < Settings.NUMBER_OF_TABLES + 1; i++) {
            tables.add(new DiningTable(i));
        }
    }

    public boolean anyOccupied(){
        return tables.stream().anyMatch(DiningTable::isOccupied);
    }


    @Override
    protected void federateCoreWork() throws RTIexception {

        noticeAboutNumberOfTables(tables.size(), 1);

        while (fedamb.running || anyOccupied() ) {
            double timeToAdvance = fedamb.federateTime + timeStep;
            advanceTime(timeToAdvance);

            if (fedamb.externalEvents.size() > 0) {
                fedamb.externalEvents.sort(new ExternalEvent.ExternalEventComparator());
                for (ExternalEvent externalEvent : fedamb.externalEvents) {
                    fedamb.federateTime = externalEvent.getTime();
                    switch (externalEvent.getEventType()) {
                        case OCCUPY_TABLE:
                            int table = occupyFirstAvailableTable(externalEvent.getClient());
                            askForWaiter(table, timeToAdvance + timeStep);
                            break;
                        case END_OF_SERVICE:
                            endOfService(externalEvent.getParameter(), timeToAdvance + timeStep);
                            break;
                        case END_SIM:
                            String federateName = externalEvent.getName();
                            if(federateName.equals("WaitingQueueFederate"))
                                fedamb.running = false;
                            break;
                    }
                }
                fedamb.externalEvents.clear();
            }


            if (fedamb.grantedTime == timeToAdvance) {
                fedamb.federateTime = timeToAdvance;
            }
            rtiamb.tick();
        }
        sendEndFederate(fedamb.federateTime+timeStep);
    }

    private void endOfService(int tableId, double timestep) throws RTIexception {

        if (random.nextInt(1, 101) <= 30) {
            log("Klient ze stolika " + tableId + " prosi o kolejne zamówienie");
            askForWaiter(tableId, timestep + randomTime());
        } else {
            freeTableByTableId(tableId, timestep);
        }
    }

    private double randomTime() {
        Random r = new Random();
        return 3 + (12 * r.nextDouble());
    }

    private void askForWaiter(int tableId, double timeStep) throws RTIexception {
        SuppliedParameters parameters =
                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();
        byte[] table = EncodingHelpers.encodeInt(tableId);

        int interactionHandle = rtiamb.getInteractionClassHandle("InteractionRoot.AskForWaiter");
        int tableIdHandle = rtiamb.getParameterHandle("tableId", interactionHandle);

        parameters.add(tableIdHandle, table);

        LogicalTime time = convertTime(timeStep);
        rtiamb.sendInteraction(interactionHandle, parameters, "tag".getBytes(), time);
        log("Kelner został poproszony o obsługe stolika : " + tableId + " at time :" + time);
    }


    private void noticeAboutNumberOfTables(int amountOfFreeTables, double timeStep) throws RTIexception {
        SuppliedParameters parameters =
                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        byte[] freeTableAmount = EncodingHelpers.encodeInt(amountOfFreeTables);

        int interactionHandle = rtiamb.getInteractionClassHandle("InteractionRoot.NoticeAboutNumberOfTables");
        int freeTableAmountHandle = rtiamb.getParameterHandle("freeTableAmount", interactionHandle);

        parameters.add(freeTableAmountHandle, freeTableAmount);

        LogicalTime time = convertTime(timeStep);
        rtiamb.sendInteraction(interactionHandle, parameters, "tag".getBytes(), time);
        log("ilosc wolnych stolikow: " + amountOfFreeTables);

    }

    private void clientLeavingRestaurant(int clientId, double timeStep) throws RTIexception {
        SuppliedParameters parameters =
                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        byte[] client = EncodingHelpers.encodeInt(clientId);

        int interactionHandle = rtiamb.getInteractionClassHandle("InteractionRoot.ClientLeavingRestaurant");
        int parameterHandle = rtiamb.getParameterHandle("clientId", interactionHandle);
        parameters.add(parameterHandle, client);
        LogicalTime time = convertTime(timeStep);
        rtiamb.sendInteraction(interactionHandle, parameters, "tag".getBytes(), time);
        log("Klient :" + clientId + " opuszcza restaruacje at time " + time);

    }

    @Override
    protected void publishAndSubscribeCore() throws RTIexception {
        /////////////////////////subscribe////////////////////////////////

        int clientEnteringRestaurantHandle = rtiamb.getInteractionClassHandle("InteractionRoot.ClientEnteringRestaurant");
        fedamb.handlerMap.put("InteractionRoot.ClientEnteringRestaurant", clientEnteringRestaurantHandle);
        rtiamb.subscribeInteractionClass(clientEnteringRestaurantHandle);


        int endOfServiceHandle = rtiamb.getInteractionClassHandle("InteractionRoot.EndOfService");
        fedamb.handlerMap.put("InteractionRoot.EndOfService", endOfServiceHandle);
        rtiamb.subscribeInteractionClass(endOfServiceHandle);

        /////////////////////////publish////////////////////////////////

        int noticeAboutNumberOfTablesHandle = rtiamb.getInteractionClassHandle("InteractionRoot.NoticeAboutNumberOfTables");
        rtiamb.publishInteractionClass(noticeAboutNumberOfTablesHandle);

        int clientLeavingRestaurantHandle = rtiamb.getInteractionClassHandle("InteractionRoot.ClientLeavingRestaurant");
        rtiamb.publishInteractionClass(clientLeavingRestaurantHandle);

        int askWaiter = rtiamb.getInteractionClassHandle("InteractionRoot.AskForWaiter");
        rtiamb.publishInteractionClass(askWaiter);
    }


    public Integer occupyFirstAvailableTable(Client client) {
        for (DiningTable table : tables) {
            if (!table.isOccupied()) {
                table.occupyTable(client);
                log("Zajęto stół o numerze : " + table.getId()
                        + " w momencie: " + fedamb.federateTime + ", przez klienta o numerze: " + client.getId());
                return table.getId();
            }
        }
        return null;
    }

    public void freeTableByTableId(int tableId, double timestep) throws RTIexception {

        Client client = tables.stream()
                .filter(t -> t.getId() == tableId).findAny().get()
                .freeTable();

        log("Klient o numerze: " + client.getId() + " opuscił stolik: "
                + tableId + " w momencie: " + fedamb.federateTime);
        clientLeavingRestaurant(client.getId(), timestep);
    }

    public static void main(String[] args) {
        try {
            new RestaurantFederate().runFederate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
