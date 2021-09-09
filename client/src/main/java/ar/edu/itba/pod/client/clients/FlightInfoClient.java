package ar.edu.itba.pod.client.clients;

import ar.edu.itba.pod.interfaces.FlightAdministrationService;
import ar.edu.itba.pod.interfaces.FlightInfoService;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ar.edu.itba.pod.client.ClientUtils.DEFAULT_REGISTRY_ADDRESS;
import static ar.edu.itba.pod.client.ClientUtils.getRegistry;

public final class FlightInfoClient {
    private static final Logger logger = LoggerFactory.getLogger(FlightInfoClient.class);

    private static String fileName;
    private static String runway;
    private static String airline;
    private static final String HEADERS = "TakeOffOrders;RunwayName;FlightCode;DestinyAirport;AirlineName";

    private FlightInfoClient() {
        // static class
    }

    public static void main(final String[] args) throws RemoteException, NotBoundException {
        logger.info("Flight Info Client Started");

        final Registry registry = getRegistry(System.getProperty("serverAddress", DEFAULT_REGISTRY_ADDRESS));

        final FlightInfoService service = (FlightInfoService) registry.lookup(FlightInfoService.CANONICAL_NAME);

        fileName = System.getProperty("outFile");
        runway = System.getProperty("runway");
        airline = System.getProperty("airline");

        if(fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("'outFile' argument must be provided");
        }

        if(runway != null && !runway.isBlank() && airline != null && !airline.isBlank()){
            throw new IllegalArgumentException("You can only run one query at a time.");
        }

        try {
            getQuery(runway, airline).execute(service);
        } catch(final Exception e) {
            System.err.println(e.getMessage());
        }

        logger.info("Flight Information Client Ended");
    }

    private static FlightInfoClient.Query getQuery(String runway, String airline) {
        
        FlightInfoClient.Query query = Query.ALL_TAKEOFFS;
        
        if(runway != null && !runway.isBlank()) {
            query = Query.RUNWAYS_TAKEOFFS;
        }else if(airline != null && !airline.isBlank()){
            query = Query.AIRLINE_TAKEOFFS;
        }

        return query;
    }

    private static void allTakeOffs(final FlightInfoService service) throws RemoteException{
        List<FlightTakeOff> takeOffs = service.queryTakeOffs();
        writeOutFile(takeOffs);
    }

    private static void airlineTakeOffs(final FlightInfoService service) throws RemoteException{
        List<FlightTakeOff> takeOffs = service.queryAirlineTakeOffs(airline);
        writeOutFile(takeOffs);
    }

    private static void runwaysTakeOffs(final FlightInfoService service) throws RemoteException{
        List<FlightTakeOff> takeOffs = service.queryRunwayTakeOffs(runway);
        writeOutFile(takeOffs);
    }

    private static void writeOutFile(List<FlightTakeOff> takeOffs){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))){
            writer.write(HEADERS);

            for (FlightTakeOff takeOff : takeOffs){
                writer.write(takeOff.getOrdersWaited() + ";");
                writer.write(takeOff.getRunway() + ";");
                writer.write(takeOff.getFlight() + ";");
                writer.write(takeOff.getDestinyAirport() + ";");
                writer.write(takeOff.getAirline() + "\n");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    
    private enum Query {
        ALL_TAKEOFFS        (FlightInfoClient::allTakeOffs),
        AIRLINE_TAKEOFFS    (FlightInfoClient::airlineTakeOffs),
        RUNWAYS_TAKEOFFS    (FlightInfoClient::runwaysTakeOffs),
        ;

        private final FlightInfoClient.FlightInfoAction handler;

        Query(final FlightInfoClient.FlightInfoAction handler) {
            this.handler    = handler;
        }

        public FlightInfoClient.FlightInfoAction getHandler() {
            return handler;
        }

        public void execute(final FlightInfoService service) throws RemoteException {
            handler.execute(service);
        }
    }

    @FunctionalInterface
    private interface FlightInfoAction {
        void execute(final FlightInfoService service) throws RemoteException;
    }
}
