package ar.edu.itba.pod.client.clients;

import ar.edu.itba.pod.interfaces.FlightAdministrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import static ar.edu.itba.pod.client.ClientUtils.DEFAULT_REGISTRY_ADDRESS;
import static ar.edu.itba.pod.client.ClientUtils.getRegistry;

public final class FlightAdministrationClient {
    private static final Logger logger = LoggerFactory.getLogger(FlightAdministrationClient.class);

    private FlightAdministrationClient() {
        // static class
    }

    public static void main(final String[] args) throws RemoteException, NotBoundException {
        logger.info("Flight Administration Client Started");

        final Registry registry = getRegistry(System.getProperty("serverAddress", DEFAULT_REGISTRY_ADDRESS));

        final FlightAdministrationService service = (FlightAdministrationService) registry.lookup(FlightAdministrationService.CANONICAL_NAME);

        logger.info("Flight Administration Client Ended");
    }
}
