package ar.edu.itba.pod.client.clients;

import ar.edu.itba.pod.interfaces.FlightInfoService;
import ar.edu.itba.pod.models.FlightTakeOff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.List;

import static ar.edu.itba.pod.client.ClientUtils.DEFAULT_REGISTRY_ADDRESS;
import static ar.edu.itba.pod.client.ClientUtils.getRegistry;

public final class FlightInfoClient {
    private static final Logger logger = LoggerFactory.getLogger(FlightInfoClient.class);

    private static final String HEADER = "TakeOffOrders;RunwayName;FlightCode;DestinyAirport;AirlineName\n";

    private FlightInfoClient() {
        // static class
    }

    public static void executeClient(final FlightInfoService service, final String outPath, final String runway, final String airline) {
        try {
            final List<FlightTakeOff> takeOffs;

            if(runway == null && airline == null) {
                takeOffs = service.queryTakeOffs();
            } else if(runway != null) {
                takeOffs = service.queryRunwayTakeOffs(runway);
            } else {
                takeOffs = service.queryAirlineTakeOffs(airline);
            }

            writeOutFile(takeOffs, outPath);
        } catch(final Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void main(final String[] args) throws RemoteException, NotBoundException {
        logger.info("Flight Info Client Started");

        final String outPath = System.getProperty("outPath");
        final String runway   = System.getProperty("runway");
        final String airline  = System.getProperty("airline");

        if(outPath == null) {
            throw new IllegalArgumentException("'outPath' argument must be provided");
        }
        if(runway != null && airline != null){
            throw new IllegalArgumentException("You can only run one query at a time.");
        }

        final Registry registry = getRegistry(System.getProperty("serverAddress", DEFAULT_REGISTRY_ADDRESS));

        final FlightInfoService service = (FlightInfoService) registry.lookup(FlightInfoService.CANONICAL_NAME);

        executeClient(service, outPath, runway, airline);

        logger.info("Flight Information Client Ended");
    }

    private static void writeOutFile(final List<FlightTakeOff> takeOffs, final String outPath) throws IOException {
        try(final BufferedWriter writer = new BufferedWriter(new FileWriter(outPath))) {
            writer.write(HEADER);

            for(final FlightTakeOff takeOff : takeOffs) {
                writer.write(
                    String.valueOf(takeOff.getOrdersWaited()) + ';' +
                    takeOff.getRunway() + ';' +
                    takeOff.getFlight() + ';' +
                    takeOff.getDestinyAirport() + ';' +
                    takeOff.getAirline() + '\n'
                );
            }
        }
    }
}
