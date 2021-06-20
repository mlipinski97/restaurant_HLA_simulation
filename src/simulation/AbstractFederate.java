package simulation;

import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

public abstract class AbstractFederate {
    public static final String READY_TO_RUN = "ReadyToRun";

    protected RTIambassador rtiamb;
    protected AbstractAmbassador fedamb;
    protected final double timeStep = 1.0;

    private final String federateName;

    public AbstractFederate(String federateName, AbstractAmbassador fedamb) {
        this.federateName = federateName;
        this.fedamb = fedamb;
    }

    public void runFederate() throws RTIexception {

        rtiamb = RtiFactoryFactory.getRtiFactory().createRtiAmbassador();

        try {
            File fom = new File("msk-projekt.fed");
            rtiamb.createFederationExecution("ExampleFederation",
                    fom.toURI().toURL());
            log("Created Federation");
        } catch (FederationExecutionAlreadyExists exists) {
            log("Didn't create federation, it already existed");
        } catch (MalformedURLException urle) {
            log("Exception processing fom: " + urle.getMessage());
            urle.printStackTrace();
            return;
        }

        rtiamb.joinFederationExecution(federateName, "ExampleFederation", fedamb);
        log("Joined Federation as " + federateName);

        rtiamb.registerFederationSynchronizationPoint(READY_TO_RUN, null);

        while (fedamb.isAnnounced == false) {
            rtiamb.tick();
        }

        waitForUser();

        rtiamb.synchronizationPointAchieved(READY_TO_RUN);
        log("Achieved sync point: " + READY_TO_RUN + ", waiting for federation...");
        while (fedamb.isReadyToRun == false) {
            rtiamb.tick();
        }

        enableTimePolicy();

        publishAndSubscribe();

        this.federateCoreWork();

        try {
            rtiamb.resignFederationExecution(ResignAction.NO_ACTION);
        }
        catch (Exception e) {
            log("TIMEOUT");
        }
    }

    protected abstract void federateCoreWork() throws RTIexception;

    private void waitForUser() {
        log(" >>>>>>>>>> Press Enter to Continue <<<<<<<<<<");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            reader.readLine();
        } catch (Exception e) {
            log("Error while waiting for user input: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void enableTimePolicy() throws RTIexception {
        LogicalTime currentTime = convertTime(fedamb.federateTime);
        LogicalTimeInterval lookahead = convertInterval(fedamb.federateLookahead);

        this.rtiamb.enableTimeRegulation(currentTime, lookahead);

        while (fedamb.isRegulating == false) {
            rtiamb.tick();
        }

        this.rtiamb.enableTimeConstrained();

        while (fedamb.isConstrained == false) {
            rtiamb.tick();
        }
    }

    protected void advanceTime(double timeToAdvance) throws RTIexception {
        fedamb.isAdvancing = true;
        LogicalTime newTime = convertTime(timeToAdvance);
        rtiamb.timeAdvanceRequest(newTime);
        //log("total time is: " + newTime);
        while (fedamb.isAdvancing) {
            rtiamb.tick();
        }
    }

    protected LogicalTime convertTime(double time) {
        // PORTICO SPECIFIC!!
        return new DoubleTime(time);
    }

    /**
     * Same as for {@link #convertTime(double)}
     */
    private LogicalTimeInterval convertInterval(double time) {
        // PORTICO SPECIFIC!!
        return new DoubleTimeInterval(time);
    }

    protected void log(String message) {
        System.out.println(federateName+ "   : " + message);
    }

    protected abstract void publishAndSubscribeCore() throws RTIexception;

    protected void publishAndSubscribe() throws RTIexception {

        int endSimulation = rtiamb.getInteractionClassHandle("InteractionRoot.SimulationEnd");
        fedamb.handlerMap.put("InteractionRoot.SimulationEnd", endSimulation);
        rtiamb.subscribeInteractionClass(endSimulation);

        /////////////////////////publish////////////////////////////////
        int endSimulationPublish = rtiamb.getInteractionClassHandle("InteractionRoot.SimulationEnd");
        rtiamb.publishInteractionClass(endSimulationPublish);

        this.publishAndSubscribeCore();
    }

    protected void sendEndFederate(double timeStep) throws RTIexception {
        SuppliedParameters parameters =
                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        byte[] name = EncodingHelpers.encodeString(federateName);
        log(federateName + " kończy prace");
        int interactionHandle = rtiamb.getInteractionClassHandle("InteractionRoot.SimulationEnd");
        int nameHandle = rtiamb.getParameterHandle("name", interactionHandle);
        parameters.add(nameHandle, name);
        LogicalTime time = convertTime(timeStep);
        rtiamb.sendInteraction(interactionHandle,parameters, "tag".getBytes(), time);
    }

}
