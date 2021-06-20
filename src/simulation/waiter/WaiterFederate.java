package simulation.waiter;

import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import simulation.AbstractAmbassador;
import simulation.AbstractFederate;
import simulation.ExternalEvent;

import java.util.Random;
import java.util.SplittableRandom;
import java.util.stream.Collectors;

public class WaiterFederate extends AbstractFederate {


    SplittableRandom random = new SplittableRandom();
    public WaiterFederate() {
        super("WaiterFederate", new WaiterAmbassador());
    }

    @Override
    protected void federateCoreWork() throws RTIexception {

        while (fedamb.running) {
            double timeToAdvance = fedamb.federateTime + timeStep;
             advanceTime(timeToAdvance);

            if (fedamb.externalEvents.size() > 0) {
                fedamb.externalEvents.sort(new ExternalEvent.ExternalEventComparator());
                for (ExternalEvent externalEvent : fedamb.externalEvents) {
                    fedamb.federateTime = externalEvent.getTime();
                    switch (externalEvent.getEventType()) {
                        case ASK_FOR_WAITER:
                            serveForClient(externalEvent.getParameter(),timeToAdvance);
                            break;
                        case ASK_FOR_WAITER_AGAIN:
                            serveForClient(externalEvent.getParameter(),timeToAdvance);
                            break;
                        case END_SIM:
                            String federateName = externalEvent.getName();
                            if(federateName.equals("RestaurantFederate"))
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
    }

    private void serveForClient(int tableId,double timeStep) throws RTIexception {

        log("Kelner zaczął obsługe przy stoliku "
                + tableId + " at time: " + fedamb.federateTime);

        endOfService(tableId,timeStep + randomTime());
    }




    private double randomTime() {
        Random r = new Random();
        return 3 + (12 * r.nextDouble());
    }

    private void endOfService(int tableId, double timeStep) throws RTIexception {
        SuppliedParameters parameters =
                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        byte[] table = EncodingHelpers.encodeInt(tableId);

        int interactionHandle = rtiamb.getInteractionClassHandle("InteractionRoot.EndOfService");
        int parameterHandle = rtiamb.getParameterHandle("tableId", interactionHandle);

        parameters.add(parameterHandle, table);

        LogicalTime time = convertTime(timeStep);
        rtiamb.sendInteraction(interactionHandle, parameters, "tag".getBytes(), time);
    }




    @Override
    protected void publishAndSubscribeCore() throws RTIexception {


        /////////////////////////subscribe////////////////////////////////

        int askWaiter = rtiamb.getInteractionClassHandle("InteractionRoot.AskForWaiter");
        fedamb.handlerMap.put("InteractionRoot.AskForWaiter", askWaiter);
        rtiamb.subscribeInteractionClass(askWaiter);

        int askAgainWaiter = rtiamb.getInteractionClassHandle("InteractionRoot.AskAgainForWaiter");
        fedamb.handlerMap.put("InteractionRoot.AskAgainForWaiter", askAgainWaiter);
        rtiamb.subscribeInteractionClass(askAgainWaiter);


        /////////////////////////publish////////////////////////////////

        int endOfService = rtiamb.getInteractionClassHandle("InteractionRoot.EndOfService");
        rtiamb.publishInteractionClass(endOfService);

        int askWaiterPublish = rtiamb.getInteractionClassHandle("InteractionRoot.AskAgainForWaiter");
        rtiamb.publishInteractionClass(askWaiterPublish);
    }




    public static void main(String[] args) {
        try {
            new WaiterFederate().runFederate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
