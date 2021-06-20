package simulation;

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.EventRetractionHandle;
import hla.rti.LogicalTime;
import hla.rti.ReceivedInteraction;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.NullFederateAmbassador;
import org.portico.impl.hla13.types.DoubleTime;
import simulation.model.Client;
import simulation.statistics.StatisticsAmbasador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractAmbassador extends NullFederateAmbassador {

    public double federateTime        = 0.0;
    public double grantedTime         = 0.0;
    public double federateLookahead   = 1.0;

    public boolean isRegulating       = false;
    public boolean isConstrained      = false;
    public boolean isAdvancing        = false;

    public boolean isAnnounced        = false;
    public boolean isReadyToRun       = false;

    public boolean running 			 = true;

    public double timeLastPatientGoToDoctor = 0;
    public static final String READY_TO_RUN = "ReadyToRun";
    /**
     * key is federate name, value is flag
     * eg. this is flag registrationIsOpen
     * for convention flag contains information if federate is open to communicate
     */

    public Map<String, Boolean> isFederateOpenMap = new HashMap<>();

    /**
     * key is interaction name, value is interaction handle
     */
    public Map<String, Integer> handlerMap = new HashMap<>();
    {
        handlerMap.put("InteractionRoot.SimulationEnd", 0);
    }


    public ArrayList<ExternalEvent> externalEvents = new ArrayList<>();

    protected double convertTime(LogicalTime logicalTime )
    {
        // PORTICO SPECIFIC!!
        return ((DoubleTime)logicalTime).getTime();
    }

    protected void log( String message )
    {
        System.out.println( "FederateAmbassador: " + message );
    }

    public void synchronizationPointRegistrationFailed( String label )
    {
        log( "Failed to register sync point: " + label );
    }

    public void synchronizationPointRegistrationSucceeded( String label )
    {
        log( "Successfully registered sync point: " + label );
    }

    public void announceSynchronizationPoint( String label, byte[] tag )
    {
        log( "Synchronization point announced: " + label );
        if( label.equals(READY_TO_RUN) )
            this.isAnnounced = true;
    }

    public void federationSynchronized( String label )
    {
        log( "Federation Synchronized: " + label );
        if( label.equals(READY_TO_RUN) )
            this.isReadyToRun = true;
    }

    /**
     * The RTI has informed us that time regulation is now enabled.
     */
    public void timeRegulationEnabled( LogicalTime theFederateTime )
    {
        this.federateTime = convertTime( theFederateTime );
        this.isRegulating = true;
    }

    public void timeConstrainedEnabled( LogicalTime theFederateTime )
    {
        this.federateTime = convertTime( theFederateTime );
        this.isConstrained = true;
    }

    public void timeAdvanceGrant( LogicalTime theTime )
    {
        this.grantedTime = convertTime( theTime );
        this.isAdvancing = false;
    }

    public void receiveInteraction( int interactionClass,
                                    ReceivedInteraction theInteraction,
                                    byte[] tag )
    {
        // just pass it on to the other method for printing purposes
        // passing null as the time will let the other method know it
        // it from us, not from the RTI
        receiveInteraction(interactionClass, theInteraction, tag, null, null);
    }

    public void receiveInteraction(int interactionClass,
                                   ReceivedInteraction theInteraction,
                                   byte[] tag,
                                   LogicalTime theTime,
                                   EventRetractionHandle eventRetractionHandle) {
        StringBuilder builder = new StringBuilder("Interaction Received:");

        if (interactionClass == handlerMap.get("InteractionRoot.SimulationEnd")) {
            try {

            String name = EncodingHelpers.decodeString(theInteraction.getValue(0));
            double time = convertTime(theTime);
            externalEvents.add(new ExternalEvent(name,
                    ExternalEvent.EventType.END_SIM,
                    time));

            builder.append("Otrzymano informacje o końcu pracy federata :").append(name);
            } catch (ArrayIndexOutOfBounds arrayIndexOutOfBounds) {
                arrayIndexOutOfBounds.printStackTrace();
            }

        }

        this.coreWork(interactionClass, theInteraction, tag, theTime, eventRetractionHandle, builder);
        if(!(this instanceof StatisticsAmbasador)) {
            log(builder.toString());
        }
    }

    public abstract void coreWork(int interactionClass,
                                  ReceivedInteraction theInteraction,
                                  byte[] tag,
                                  LogicalTime theTime,
                                  EventRetractionHandle eventRetractionHandle,
                                  StringBuilder builder);
}
