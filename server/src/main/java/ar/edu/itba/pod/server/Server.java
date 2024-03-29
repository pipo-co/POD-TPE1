package ar.edu.itba.pod.server;

import ar.edu.itba.pod.interfaces.FlightAdministrationService;
import ar.edu.itba.pod.interfaces.FlightInfoService;
import ar.edu.itba.pod.interfaces.FlightRunwayRequestService;
import ar.edu.itba.pod.interfaces.FlightTrackingService;
import ar.edu.itba.pod.server.repositories.AwaitingFlightsRepository;
import ar.edu.itba.pod.server.repositories.FlightRunwayRepository;
import ar.edu.itba.pod.server.repositories.FlightTakeOffRepository;
import ar.edu.itba.pod.server.repositories.impls.InMemoryAwaitingFlightsRepository;
import ar.edu.itba.pod.server.repositories.impls.InMemoryFlightRunwayRepository;
import ar.edu.itba.pod.server.repositories.impls.InMemoryFlightTakeOffRepository;
import ar.edu.itba.pod.server.services.AllInOneServiceImpl;
import ar.edu.itba.pod.server.services.FlightAdministrationServiceImpl;
import ar.edu.itba.pod.server.services.FlightInfoServiceImpl;
import ar.edu.itba.pod.server.services.FlightRunwayRequestServiceImpl;
import ar.edu.itba.pod.server.services.FlightTrackingServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Executors;

public final class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private Server() {
        // static class
    }

    private static final String DEFAULT_REGISTRY_HOST       = "localhost";
    private static final int    DEFAULT_REGISTRY_PORT       = Registry.REGISTRY_PORT;
    private static final char   ADDRESS_DELIM               = ':';
    private static final String DEFAULT_REGISTRY_ADDRESS    = DEFAULT_REGISTRY_HOST + ADDRESS_DELIM + DEFAULT_REGISTRY_PORT;

    public static void main(final String[] args) throws RemoteException {
        logger.info("Server Started");

        final String address = System.getProperty("registryAddress", DEFAULT_REGISTRY_ADDRESS);
        final int addressDelimIdx = address.indexOf(ADDRESS_DELIM);

        final String    host = addressDelimIdx >= 0 ? address.substring(0, addressDelimIdx)                     : DEFAULT_REGISTRY_HOST;
        final int       port = addressDelimIdx >= 0 ? Integer.parseInt(address.substring(addressDelimIdx + 1))  : DEFAULT_REGISTRY_PORT;

        final Registry registry = LocateRegistry.getRegistry(host, port);
        logger.info("Registry Found");

        final FlightRunwayRepository    flightRunwayRepository      = new InMemoryFlightRunwayRepository(Executors.newCachedThreadPool());
        final FlightTakeOffRepository   flightTakeOffRepository     = new InMemoryFlightTakeOffRepository();
        final AwaitingFlightsRepository awaitingFlightsRepository   = new InMemoryAwaitingFlightsRepository();

        final AllInOneServiceImpl allInOneService = new AllInOneServiceImpl(
            new FlightAdministrationServiceImpl(flightRunwayRepository, flightTakeOffRepository, awaitingFlightsRepository),
            new FlightInfoServiceImpl(flightTakeOffRepository),
            new FlightRunwayRequestServiceImpl(flightRunwayRepository, awaitingFlightsRepository),
            new FlightTrackingServiceImpl(awaitingFlightsRepository)
        );

        final Remote servant = UnicastRemoteObject.exportObject(allInOneService, 0);

        registry.rebind(FlightAdministrationService .CANONICAL_NAME, servant);
        logger.info("Flight Administration Service Registered");

        registry.rebind(FlightInfoService           .CANONICAL_NAME, servant);
        logger.info("Flight Information Service Registered");

        registry.rebind(FlightTrackingService       .CANONICAL_NAME, servant);
        logger.info("Flight Tracking Service Registered");

        registry.rebind(FlightRunwayRequestService  .CANONICAL_NAME, servant);
        logger.info("Flight Runway Request Service Registered");

        logger.info("All Services Registered - Awaiting Requests ...");
    }
}
