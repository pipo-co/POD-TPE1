package ar.edu.itba.pod.client.clients;

import ar.edu.itba.pod.callbacks.FlightRunwayEventConsumer;
import ar.edu.itba.pod.interfaces.FlightTrackingService;
import ar.edu.itba.pod.models.FlightRunwayEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import static ar.edu.itba.pod.client.ClientUtils.*;

public final class FlightTrackingClient {
    private static final Logger logger = LoggerFactory.getLogger(FlightTrackingClient.class);

    private FlightTrackingClient() {
        // static class
    }

    public static void main(final String[] args) throws RemoteException, NotBoundException {
        logger.info("Flight Tracking Client Started");

        final Registry registry = getRegistry(System.getProperty("serverAddress", DEFAULT_REGISTRY_ADDRESS));

        final String airline = System.getProperty("airlineName");
        if(airline == null) {
            throw new IllegalArgumentException("Argument 'airlineName' is mandatory");
        }

        final String flightCode = System.getProperty("flightCode");
        if(flightCode == null || flightCode.isBlank()) {
            throw new IllegalArgumentException("'outFile' argument must be provided");
        }

        final FlightTrackingService service = (FlightTrackingService) registry.lookup(FlightTrackingService.CANONICAL_NAME);

        final FlightRunwayEventConsumer callback = new RunwayEventCallback();
        UnicastRemoteObject.exportObject(callback, 0);

        service.suscribeToFlight(airline, flightCode, callback);

        logger.info("Flight Tracking Client Ended");
    }

    private static final class RunwayEventCallback implements FlightRunwayEventConsumer {

        @Override
        public void accept(final FlightRunwayEvent e) throws RemoteException {
            final String out;
            switch(e.getType()) {
                case RUNWAY_ASSIGNMENT:
                    out = "Flight " + e.getFlight()
                        + " with destiny " + e.getDestination() + " was assigned to runway " + e.getRunway()
                        + " and there are " + e.getPosition() + " flights waiting ahead."
                        ;
                    break;
                case RUNWAY_PROGRESS:
                    out = "A flight departed from runway " + e.getRunway()
                        + ". Flight " + e.getFlight() + " with destiny " + e.getDestination() + " has "
                        + e.getPosition() + " flights waiting ahead."
                        ;
                    break;
                case FLIGHT_TAKE_OFF:
                    out ="Flight " + e.getFlight() + " with destiny "
                        + e.getDestination() + " departed on runway " + e.getRunway() + "."
                        ;
                    // Como el avion ya despego, des-exportamos el callback
                    // Como necesitamos la autoreferencia, no podemos usar lambda :(
                    UnicastRemoteObject.unexportObject(this, true);
                    break;
                default:
                    throw new IllegalStateException();
            }

            System.out.println(out);
        }
    }
}
