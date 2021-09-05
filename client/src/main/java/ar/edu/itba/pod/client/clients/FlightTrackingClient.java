package ar.edu.itba.pod.client.clients;

import ar.edu.itba.pod.interfaces.FlightTrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import static ar.edu.itba.pod.client.ClientUtils.*;

public final class FlightTrackingClient {
    private static final Logger logger = LoggerFactory.getLogger(FlightTrackingClient.class);

    private FlightTrackingClient() {
        // static class
    }

    public static void main(final String[] args) throws RemoteException, NotBoundException {
        logger.info("Flight Tracking Client Started");

        final Registry registry = getRegistry(System.getProperty("serverAddress", DEFAULT_REGISTRY_ADDRESS));

        final String airline = System.getProperty("airline");
        final String flightCode = System.getProperty("flightCode");

        if (airline == null || flightCode == null){
            throw new IllegalArgumentException("Arguments airline and flightCode are needed.");
        }

        final FlightTrackingService service = (FlightTrackingService) registry.lookup(FlightTrackingService.CANONICAL_NAME);

        service.suscribeToFlight(airline, flightCode, e -> {
            switch (e.getType()){
                case RUNWAY_ASSIGNMENT:
                    System.out.println("Flight "+ e.getFlight()
                            +" with destiny "+e.getDestination()+" was assigned to runway "+e.getRunway()
                            +" and there are "+e.getPosition()+"flights waiting ahead.");
                case RUNWAY_PROGRESS:
                    System.out.println("A flight departed from runway "+e.getRunway()
                            +". Flight "+e.getFlight()+" with destiny "+e.getDestination()+" has "
                            +e.getPosition()+"flights waiting ahead.");
                case FLIGHT_TAKE_OFF:
                    System.out.println("Flight "+e.getFlight()+" with destiny "
                            +e.getDestination()+" departed on runway " +e.getRunway());
            }
        });

        logger.info("Flight Tracking Client Ended");
    }
}
