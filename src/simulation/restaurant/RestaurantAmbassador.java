package simulation.restaurant;

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.EventRetractionHandle;
import hla.rti.LogicalTime;
import hla.rti.ReceivedInteraction;
import hla.rti.jlc.EncodingHelpers;
import simulation.AbstractAmbassador;
import simulation.ExternalEvent;
import simulation.model.Client;

public class RestaurantAmbassador extends AbstractAmbassador {


    public RestaurantAmbassador() {
        handlerMap.put("InteractionRoot.ClientEnteringRestaurant", 0);
        handlerMap.put("InteractionRoot.ClientLeavingRestaurant", 0);
        handlerMap.put("InteractionRoot.EndOfService", 0);
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
                builder.append(" Klient: ")
                        .append(clientId)
                        .append(" wszedl do restauracji w momencie = ")
                        .append(time)
                        .append("\n");

            } catch (ArrayIndexOutOfBounds ignored) {
            }
        }else if (interactionClass == handlerMap.get("InteractionRoot.EndOfService")) {
            try {
                int tableId = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                double time = convertTime(theTime);
                externalEvents.add(new ExternalEvent(
                        ExternalEvent.EventType.END_OF_SERVICE,
                        time,tableId));
                builder.append(" Kelner zakończył obsługe stolika nr : ")
                        .append(tableId)
                        .append(" w momencie = ")
                        .append(time)
                        .append("\n");
            } catch (ArrayIndexOutOfBounds ignored) {
            }
        }
    }

}
