package ar.edu.itba.pod.client;

import ar.edu.itba.pod.interfaces.FlightTrackRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public final class FlightTrackRequestClient {
    private static final Logger logger = LoggerFactory.getLogger(FlightTrackRequestClient.class);

    private FlightTrackRequestClient() {
        // static class
    }

    public static void main(final String[] args) throws RemoteException, NotBoundException {
        logger.info("Flight Track Request Client Started");
        final Registry registry = LocateRegistry.getRegistry("localhost");
        final FlightTrackRequestService service = (FlightTrackRequestService) registry.lookup(FlightTrackRequestService.CANONICAL_NAME);
        logger.info("Flight Track Request Client Ended");
    }
}
