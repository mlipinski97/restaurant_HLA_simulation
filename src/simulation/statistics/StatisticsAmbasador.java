package simulation.statistics;

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.EventRetractionHandle;
import hla.rti.LogicalTime;
import hla.rti.ReceivedInteraction;
import hla.rti.jlc.EncodingHelpers;
import simulation.AbstractAmbassador;
import simulation.ExternalEvent;
import simulation.model.Client;

public class StatisticsAmbasador extends AbstractAmbassador {

    public StatisticsAmbasador() {
        handlerMap.put("InteractionRoot.ClientArrival", 0);
        handlerMap.put("InteractionRoot.ClientEnteringRestaurant", 0);
    }

    @Override
    public void coreWork(int interactionClass, ReceivedInteraction theInteraction, byte[] tag, LogicalTime theTime, EventRetractionHandle eventRetractionHandle, StringBuilder builder) {

        if (interactionClass == handlerMap.get("InteractionRoot.ClientEnteringRestaurant")) {
            try {
                int clientId = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                double time = convertTime(theTime);
                externalEvents.add(new ExternalEvent(new Client(clientId),
                        ExternalEvent.EventType.OCCUPY_TABLE,
                        time));

            } catch (ArrayIndexOutOfBounds ignored) {
            }
        }else if(interactionClass == handlerMap.get("InteractionRoot.ClientArrival")) {
            try {
                int clientId = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                double time = convertTime(theTime);
                externalEvents.add(new ExternalEvent(new Client(clientId),
                        ExternalEvent.EventType.CLIENT_ARRIVAL,
                        time));


            } catch (ArrayIndexOutOfBounds ignored) {
            }

        }else if(interactionClass == handlerMap.get("InteractionRoot.ClientLeavingRestaurant")) {
            try {
                int clientId = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                double time = convertTime(theTime);
                externalEvents.add(new ExternalEvent(new Client(clientId),
                        ExternalEvent.EventType.CLIENT_LEAVING_RESTAURANT,
                        time));

            } catch (ArrayIndexOutOfBounds ignored) {
            }

        }

    }
}
