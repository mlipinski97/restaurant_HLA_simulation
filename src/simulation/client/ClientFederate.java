package simulation.client;

import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import simulation.AbstractAmbassador;
import simulation.AbstractFederate;
import simulation.model.Client;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;

import static simulation.Settings.endSimulationTime;

public class ClientFederate extends AbstractFederate {

//    public static final String READY_TO_RUN = "ReadyToRun";


    private int numberOfGeneratedClients;
    private List<Client> clients = new ArrayList<>();
    SplittableRandom random = new SplittableRandom();

    public ClientFederate() {
        super("ClientFederate", new ClientAmbassador());
    }

    @Override
    protected void federateCoreWork() throws RTIexception {
        while (fedamb.running) {
            double timeToAdvance = fedamb.federateTime + randomTime();
            advanceTime(timeToAdvance);
            if (fedamb.grantedTime == timeToAdvance) {
                sendClientArrivalInteraction(timeToAdvance+ timeStep);
                //dajemy 10% na to ze ktos sie wkurzy i sobie pojdzie
                if (random.nextInt(1,101) <= 10) {
                    sendClientImpatienceInteraction(timeToAdvance+ timeStep);
                }
                rtiamb.tick();
                fedamb.federateTime = timeToAdvance;

                if(timeToAdvance>endSimulationTime){
                    sendEndFederate(timeToAdvance+ timeStep);
                    fedamb.running = false;
                }
            }
        }
    }

    @Override
    protected void publishAndSubscribeCore() throws RTIexception {
        int clientArrivalHandle = rtiamb.getInteractionClassHandle("InteractionRoot.ClientArrival");
        rtiamb.publishInteractionClass(clientArrivalHandle);

        int ClientLeavingQueueHandle = rtiamb.getInteractionClassHandle("InteractionRoot.ClientLeavingQueue");
        rtiamb.publishInteractionClass(ClientLeavingQueueHandle);

    }

    private double randomTime() {
        Random r = new Random();
        return 3 + (4 * r.nextDouble());
    }

    private void sendClientArrivalInteraction(double timeStep) throws RTIexception {
        SuppliedParameters parameters =
                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        byte[] clientId = EncodingHelpers.encodeInt(++numberOfGeneratedClients);

        int interactionHandle = rtiamb.getInteractionClassHandle("InteractionRoot.ClientArrival");
        int clientIdHandle = rtiamb.getParameterHandle("clientId", interactionHandle);

        parameters.add(clientIdHandle, clientId);

        LogicalTime time = convertTime(timeStep);
        log("Klient o numerze: " + numberOfGeneratedClients + " dołączył do kolejki w momencie "+ timeStep);
        rtiamb.sendInteraction(interactionHandle, parameters, "tag".getBytes(), time);
        clients.add(new Client(numberOfGeneratedClients));
    }

    private void sendClientImpatienceInteraction(double timeStep) throws RTIexception  {
        SuppliedParameters parameters =
                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();
        int indexOfImpatientClient = random.nextInt(clients.size());
        byte[] clientId = EncodingHelpers.encodeInt(clients.get(indexOfImpatientClient).getId());

        int interactionHandle = rtiamb.getInteractionClassHandle("InteractionRoot.clientLeavingQueue");
        int clientIdHandle = rtiamb.getParameterHandle("clientId", interactionHandle);

        parameters.add(clientIdHandle, clientId);

        LogicalTime time = convertTime(timeStep);
        rtiamb.sendInteraction(interactionHandle, parameters, "tag".getBytes(), time);
        clients.remove(indexOfImpatientClient);
    }

    public static void main(String[] args) {
        try {
            new ClientFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }
}
