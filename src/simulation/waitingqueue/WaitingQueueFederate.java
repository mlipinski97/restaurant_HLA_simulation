package simulation.waitingqueue;

import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import simulation.AbstractFederate;
import simulation.ExternalEvent;
import simulation.model.Client;
import simulation.model.WaitingQueue;

import java.util.SplittableRandom;

public class WaitingQueueFederate extends AbstractFederate {


    private WaitingQueue queue = new WaitingQueue();
    private int numberOfFreeTables;
    SplittableRandom random = new SplittableRandom();


    public WaitingQueueFederate() {
        super("WaitingQueueFederate", new WaitingQueueAmbassador());
    }


    @Override
    protected void federateCoreWork() throws RTIexception {

        while (fedamb.running || queue.getQueueSize()>0) {
            double timeToAdvance = fedamb.federateTime + timeStep;
            advanceTime(timeToAdvance);

            if (fedamb.externalEvents.size() > 0) {
                fedamb.externalEvents.sort(new ExternalEvent.ExternalEventComparator());
                for (ExternalEvent externalEvent : fedamb.externalEvents) {
                    fedamb.federateTime = externalEvent.getTime();
                    switch (externalEvent.getEventType()) {
                        case CLIENT_ARRIVAL:
                            this.noticeClientArrival(externalEvent.getClient());
                            break;
                        case CLIENT_LEAVING_QUEUE:
                            this.noticeClientLeaving(externalEvent.getClient());
                            break;
                        case NOTICE_ABOUT_AMOUNT_OF_TABLES:
                            numberOfFreeTables = externalEvent.getParameter();
                            break;
                        case TABLE_BEING_FREED:
                            numberOfFreeTables++;
                            break;
                        case END_SIM:
                            String federateName = externalEvent.getName();
                            if(federateName.equals("ClientFederate"))
                                fedamb.running = false;
                            break;
                    }
                }
                fedamb.externalEvents.clear();
            }

            if (fedamb.grantedTime == timeToAdvance) {
                timeToAdvance += fedamb.federateLookahead;
                while (numberOfFreeTables > 0 && queue.getQueueSize() > 0) {
                    sendClientToRestaurantInteraction(timeToAdvance + timeStep);
                    numberOfFreeTables--;
                }
                fedamb.federateTime = timeToAdvance;
            }
            rtiamb.tick();
        }
        sendEndFederate(fedamb.federateTime+timeStep);
    }

    @Override
    protected void publishAndSubscribeCore() throws RTIexception {
        /////////////////////////subscribe////////////////////////////////
        int clientArrivalHandle = rtiamb.getInteractionClassHandle("InteractionRoot.ClientArrival");
        fedamb.handlerMap.put("InteractionRoot.ClientArrival", clientArrivalHandle);
        rtiamb.subscribeInteractionClass(clientArrivalHandle);

        int clientLeavingQueueHandle = rtiamb.getInteractionClassHandle("InteractionRoot.ClientLeavingQueue");
        fedamb.handlerMap.put("InteractionRoot.ClientLeavingQueue", clientLeavingQueueHandle);
        rtiamb.subscribeInteractionClass(clientLeavingQueueHandle);

        int noticeAboutNumberOfTables = rtiamb.getInteractionClassHandle("InteractionRoot.NoticeAboutNumberOfTables");
        fedamb.handlerMap.put("InteractionRoot.NoticeAboutNumberOfTables", noticeAboutNumberOfTables);
        rtiamb.subscribeInteractionClass(noticeAboutNumberOfTables);

        int clientLeavingRestaurantHandle = rtiamb.getInteractionClassHandle("InteractionRoot.ClientLeavingRestaurant");
        fedamb.handlerMap.put("InteractionRoot.ClientLeavingRestaurant", clientLeavingRestaurantHandle);
        rtiamb.subscribeInteractionClass(clientLeavingRestaurantHandle);

        /////////////////////////publish////////////////////////////////

        int clientEnteringRestaurantHandle = rtiamb.getInteractionClassHandle("InteractionRoot.ClientEnteringRestaurant");
        rtiamb.publishInteractionClass(clientEnteringRestaurantHandle);

    }


    
    private void noticeClientArrival(Client client) {
        queue.addClientToQueue(client);
        log("Klient o numerze: " + client.getId() + " dołączył do kolejki w momencie " + fedamb.federateTime
                + "obecna ilość osób w kolejce: " + queue.getQueueSize());

    }

    private void noticeClientLeaving(Client client) {
        if(queue.getQueueSize() < 1 )return;
        Client clientToLeave = new Client(random.nextInt(queue.getQueueSize()));
        queue.removeChosenClientFromQueue(clientToLeave);
        log("Klient o numerze: " + client.getId() + " znierciepliwił sie i wyszedł w momencie " + fedamb.federateTime
                + "obecna ilość osób w kolejce: " + queue.getQueueSize());
    }


    private void sendClientToRestaurantInteraction(double timeStep) throws RTIexception {
        SuppliedParameters parameters =
                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        Client client = queue.removeFirstClientFromQueue();
        log("Client number: " + client.getId() + " left the queue and entered simulation.restaurant current number of clients in queue: "
                + queue.getQueueSize());
        byte[] clientId = EncodingHelpers.encodeInt(client.getId());

        int interactionHandle = rtiamb.getInteractionClassHandle("InteractionRoot.ClientEnteringRestaurant");
        int clientIdHandle = rtiamb.getParameterHandle("clientId", interactionHandle);

        parameters.add(clientIdHandle, clientId);

        LogicalTime time = convertTime(timeStep);
        rtiamb.sendInteraction(interactionHandle, parameters, "tag".getBytes(), time);

    }

    public static void main(String[] args) {
        try {
            new WaitingQueueFederate().runFederate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
