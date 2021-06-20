package simulation.client;

import hla.rti.EventRetractionHandle;
import hla.rti.LogicalTime;
import hla.rti.ReceivedInteraction;
import hla.rti.jlc.NullFederateAmbassador;
import org.portico.impl.hla13.types.DoubleTime;
import simulation.AbstractAmbassador;
import simulation.AbstractFederate;

public class ClientAmbassador extends AbstractAmbassador {

    @Override
    public void coreWork(int interactionClass, ReceivedInteraction theInteraction, byte[] tag, LogicalTime theTime, EventRetractionHandle eventRetractionHandle, StringBuilder builder) {

    }
}
