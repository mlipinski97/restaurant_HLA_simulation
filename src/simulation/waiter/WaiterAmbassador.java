package simulation.waiter;

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.EventRetractionHandle;
import hla.rti.LogicalTime;
import hla.rti.ReceivedInteraction;
import hla.rti.jlc.EncodingHelpers;
import simulation.AbstractAmbassador;
import simulation.ExternalEvent;
import simulation.model.Client;

public class WaiterAmbassador extends AbstractAmbassador {

    public WaiterAmbassador() {
        handlerMap.put("InteractionRoot.AskForWaiter", 0);
        handlerMap.put("InteractionRoot.AskAgainForWaiter", 0);

    }

    @Override
    public void coreWork(int interactionClass, ReceivedInteraction theInteraction, byte[] tag, LogicalTime theTime, EventRetractionHandle eventRetractionHandle, StringBuilder builder) {

        if (interactionClass == handlerMap.get("InteractionRoot.AskForWaiter")) {
            try {
                int tableId = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                double time = convertTime(theTime);
                externalEvents.add(new ExternalEvent(
                        ExternalEvent.EventType.ASK_FOR_WAITER,
                        time,tableId));

            } catch (ArrayIndexOutOfBounds ignored) {

            }

        }

    }
}
