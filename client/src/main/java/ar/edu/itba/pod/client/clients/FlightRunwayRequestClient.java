package ar.edu.itba.pod.client.clients;

import ar.edu.itba.pod.client.models.RunwayRequest;
import ar.edu.itba.pod.exceptions.UniqueFlightCodeConstraintException;
import ar.edu.itba.pod.exceptions.UnregistrableFlightException;
import ar.edu.itba.pod.interfaces.FlightRunwayRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.stream.Stream;

import static ar.edu.itba.pod.client.ClientUtils.DEFAULT_REGISTRY_ADDRESS;
import static ar.edu.itba.pod.client.ClientUtils.getRegistry;

public final class FlightRunwayRequestClient {
    private static final Logger logger = LoggerFactory.getLogger(FlightRunwayRequestClient.class);

    private FlightRunwayRequestClient() {
        // static class
    }

    public static void main(final String[] args) throws IOException, NotBoundException {
        logger.info("Flight Runway Request Client Started");

        final Registry registry = getRegistry(System.getProperty("serverAddress", DEFAULT_REGISTRY_ADDRESS));

        final String csvPath = System.getProperty("inPath");
        if(csvPath == null) {
            throw new IllegalArgumentException("Missing 'inPath' system property pointing to csv input file");
        }
        final Path csv = Path.of(csvPath);
        if(!Files.isRegularFile(csv)) {
            throw new IllegalArgumentException("'inPath' must point to a regular file");
        }

        final FlightRunwayRequestService service = (FlightRunwayRequestService) registry.lookup(FlightRunwayRequestService.CANONICAL_NAME);

        try(final Stream<String> runwayRequestLines = Files.lines(csv)) {
            final long totalRequests = runwayRequestLines
                .map(RunwayRequest::fromCSV)
                .map(request -> registerFlight(service, request))
                .filter(x -> x)
                .count()
                ;

            System.out.println(totalRequests + " flights assigned.");
        }

        logger.info("Flight Runway Request Client Ended");
    }

    private static boolean registerFlight(final FlightRunwayRequestService service, final RunwayRequest request) {
        boolean ret = true;

        try {
            service.registerFlight(request.getFlightCode(), request.getAirlineName(), request.getDestinyAirport(), request.getCategory());
        } catch(final UnregistrableFlightException e) {
            System.err.printf("Cannot assign Flight %s.", request.getFlightCode());
            ret = false;
        } catch(final UniqueFlightCodeConstraintException e) {
            System.err.printf("There already exists a registered flight with code %s.%n", request.getFlightCode());
            ret = false;
        } catch(final RemoteException e) {
            System.err.println(e.getMessage());
            ret = false;
        }

        return ret;
    }
}
