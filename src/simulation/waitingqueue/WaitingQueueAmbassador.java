package simulation.waitingqueue;

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.EventRetractionHandle;
import hla.rti.LogicalTime;
import hla.rti.ReceivedInteraction;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.NullFederateAmbassador;
import simulation.AbstractAmbassador;
import simulation.ExternalEvent;
import simulation.model.Client;
import org.portico.impl.hla13.types.DoubleTime;

import java.util.ArrayList;

public class WaitingQueueAmbassador extends AbstractAmbassador {

    ///protected ArrayList<WaitingQueueExternalEvent> externalEvents = new ArrayList<>();

    public WaitingQueueAmbassador() {
        handlerMap.put("InteractionRoot.ClientArrival", 0);
        handlerMap.put("InteractionRoot.ClientLeavingQueue", 0);
        handlerMap.put("InteractionRoot.NoticeAboutNumberOfTables", 0);
        handlerMap.put("InteractionRoot.ClientLeavingRestaurant", 0);

    }

    @Override
    public void coreWork(int interactionClass, ReceivedInteraction theInteraction, byte[] tag, LogicalTime theTime, EventRetractionHandle eventRetractionHandle, StringBuilder builder) {
        if (interactionClass == handlerMap.get("InteractionRoot.ClientArrival")) {
            try {
                int clientId = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                double time = convertTime(theTime);
                externalEvents.add(new ExternalEvent(new Client(clientId),
                        ExternalEvent.EventType.CLIENT_ARRIVAL,
                        time));
                builder.append("simulation.client arrival , time=")
                        .append(time)
                        .append(" simulation.client id = ").append(clientId)
                        .append("\n");

            } catch (ArrayIndexOutOfBounds ignored) {

            }

        } else if (interactionClass == handlerMap.get("InteractionRoot.ClientLeavingQueue")) {
            try {
                int clientId = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                double time = convertTime(theTime);
                externalEvents.add(new ExternalEvent(new Client(clientId),
                        ExternalEvent.EventType.CLIENT_LEAVING_QUEUE,
                        time));
                builder.append("simulation.client leaving , time=")
                        .append(time)
                        .append(" simulation.client id = ").append(clientId)
                        .append("\n");

            } catch (ArrayIndexOutOfBounds ignored) {

            }
        } else if (interactionClass == handlerMap.get("InteractionRoot.NoticeAboutNumberOfTables")) {
            try {
                int amountOfFreeTables = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                double time = convertTime(theTime);
                externalEvents.add(new ExternalEvent(ExternalEvent.EventType.NOTICE_ABOUT_AMOUNT_OF_TABLES,
                        time,
                        amountOfFreeTables));
                builder.append("time=")
                        .append(time)
                        .append(" simulation.restaurant has: ")
                        .append(amountOfFreeTables)
                        .append("tables that night")
                        .append("\n");

            } catch (ArrayIndexOutOfBounds ignored) {

            }
        } else if(interactionClass == handlerMap.get("InteractionRoot.ClientLeavingRestaurant")){
            try {
                int clientId = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                double time = convertTime(theTime);
                //tutaj simulation.client nie ma znaczenia bo i tak zwiekszamy tylko ilosc wolnych stolikow
                externalEvents.add(new ExternalEvent(new Client(clientId),
                        ExternalEvent.EventType.TABLE_BEING_FREED,
                        time));
                builder.append(" - number of free tables increased ")
                        .append("time=")
                        .append(time)
                        .append("\n");
            } catch (ArrayIndexOutOfBounds ignored) {

            }
        }
    }

}
