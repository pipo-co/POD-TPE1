package ar.edu.itba.pod.client.clients;

import static ar.edu.itba.pod.client.ClientUtils.DEFAULT_REGISTRY_ADDRESS;
import static ar.edu.itba.pod.client.ClientUtils.getRegistry;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.edu.itba.pod.callbacks.FlightRunwayEventConsumer;
import ar.edu.itba.pod.exceptions.AirlineFlightMismatchException;
import ar.edu.itba.pod.exceptions.FlightNotFoundException;
import ar.edu.itba.pod.interfaces.FlightTrackingService;
import ar.edu.itba.pod.models.FlightRunwayEvent;

public final class FlightTrackingClient {
    private static final Logger logger = LoggerFactory.getLogger(FlightTrackingClient.class);

    private FlightTrackingClient() {
        // static class
    }

    public static void executeClient(final FlightTrackingService service, final String airline, final String flightCode, final FlightRunwayEventConsumer callback) throws RemoteException {
        try {
            service.suscribeToFlight(airline, flightCode, callback);
        } catch (FlightNotFoundException e) {
            throw new RuntimeException("Flight not found. It either wasn't registered yet or has already departed.");
        } catch (AirlineFlightMismatchException e) {
            throw new RuntimeException("This flight doesn't match the specified airline.");
        }
    }

    public static void main(final String[] args) throws RemoteException, NotBoundException {
        logger.info("Flight Tracking Client Started");

        final Registry registry = getRegistry(System.getProperty("serverAddress", DEFAULT_REGISTRY_ADDRESS));

        final String airline = System.getProperty("airline");
        if(airline == null) {
            throw new IllegalArgumentException("Argument 'airline' is mandatory");
        }

        final String flightCode = System.getProperty("flightCode");
        if(flightCode == null || flightCode.isBlank()) {
            throw new IllegalArgumentException("'outFile' argument must be provided");
        }

        final FlightTrackingService service = (FlightTrackingService) registry.lookup(FlightTrackingService.CANONICAL_NAME);

        final FlightRunwayEventConsumer callback = new RunwayEventCallback();

        UnicastRemoteObject.exportObject(callback, 0);

        try {
            executeClient(service, airline, flightCode, callback);
        } catch(final Exception e) {
            System.err.println(e.getMessage());
            UnicastRemoteObject.unexportObject(callback, true);
        }

        logger.info("Flight Tracking Client Ended");
    }

    public static final class RunwayEventCallback implements FlightRunwayEventConsumer {

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
