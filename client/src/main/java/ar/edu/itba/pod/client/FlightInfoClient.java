package ar.edu.itba.pod.client;

import ar.edu.itba.pod.interfaces.FlightInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public final class FlightInfoClient {
    private static final Logger logger = LoggerFactory.getLogger(FlightInfoClient.class);

    private FlightInfoClient() {
        // static class
    }

    public static void main(final String[] args) throws RemoteException, NotBoundException {
        logger.info("Flight Info Client Started");
        final Registry registry = LocateRegistry.getRegistry("localhost");
        final FlightInfoService service = (FlightInfoService) registry.lookup(FlightInfoService.CANONICAL_NAME);
        logger.info("Flight Information Client Ended");
    }
}
