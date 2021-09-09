package ar.edu.itba.pod.client.clients;

import static ar.edu.itba.pod.client.ClientUtils.DEFAULT_REGISTRY_ADDRESS;
import static ar.edu.itba.pod.client.ClientUtils.getRegistry;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.edu.itba.pod.exceptions.RunwayNotFoundException;
import ar.edu.itba.pod.exceptions.UniqueRunwayNameConstraintException;
import ar.edu.itba.pod.interfaces.FlightAdministrationService;
import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.models.RunwayReorderSummary;

public final class FlightAdministrationClient {
    private static final Logger logger = LoggerFactory.getLogger(FlightAdministrationClient.class);
    
    private FlightAdministrationClient() {
        // static class
    }

    public static void executeClient(final FlightAdministrationService service, final String actionName, final String runway, final String categoryName) {
        try {
            getAction(actionName).execute(service, runway, categoryName);
        } catch(final Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void main(final String[] args) throws RemoteException, NotBoundException {
        logger.info("Flight Administration Client Started");

        final Registry registry = getRegistry(System.getProperty("serverAddress", DEFAULT_REGISTRY_ADDRESS));

        final String actionName     = System.getProperty("action");
        final String runway         = System.getProperty("runway");
        final String categoryName   = System.getProperty("category");

        final FlightAdministrationService service = (FlightAdministrationService) registry.lookup(FlightAdministrationService.CANONICAL_NAME);

        executeClient(service, actionName, runway, categoryName);

        logger.info("Flight Administration Client Ended");
    }

    public static void addRunway(final FlightAdministrationService service, final String actionName, final String runway, final String categoryName) throws RemoteException {

        validateRunway(runway);
        final FlightRunwayCategory category = getCategory(categoryName);

        final boolean status;
        try {
            status = service.createRunway(runway, category);
        } catch(final UniqueRunwayNameConstraintException e) {
            throw new RuntimeException(String.format("There already exists a runway with the name %s.", runway), e);
        }

        System.out.println(getRunwayStatusMessage(runway, status));
    }
    
    public static void open(final FlightAdministrationService service, final String actionName, final String runway, final String categoryName) throws RemoteException {
        
        validateRunway(runway);

        try {
            service.openRunway(runway);
        } catch(final RunwayNotFoundException e) {
            throw new RuntimeException(String.format("Runway with name %s not found. Make sure to call 'open' with an existing runway.", runway), e);
        }

        System.out.println(getRunwayStatusMessage(runway, true));
    }
    
    public static void close(final FlightAdministrationService service, final String actionName, final String runway, final String categoryName) throws RemoteException {
        
        validateRunway(runway);
        
        try {
            service.closeRunway(runway);
        } catch(final RunwayNotFoundException e) {
            throw new RuntimeException(String.format("Runway with name %s not found. Make sure to call 'close' with a existing runway.", runway), e);
        }

        System.out.println(getRunwayStatusMessage(runway, false));
    }
    
    public static void status(final FlightAdministrationService service, final String actionName, final String runway, final String categoryName) throws RemoteException {
        
        validateRunway(runway);
        
        final boolean status;
        try {
            status = service.isRunwayOpen(runway);
        } catch(final RunwayNotFoundException e) {
            throw new RuntimeException(String.format("Runway with name %s not found. Make sure to call 'status' with a existing runway.", runway), e);
        }

        System.out.println(getRunwayStatusMessage(runway, status));
    }
    
    public static void takeOff(final FlightAdministrationService service, final String actionName, final String runway, final String categoryName) throws RemoteException {
        service.orderTakeOff();
        System.out.println("Flights departed!");
    }
    
    public static void reorder(final FlightAdministrationService service, final String actionName, final String runway, final String categoryName) throws RemoteException {
        final RunwayReorderSummary summary = service.reorderRunways();

        summary.getUnassignedFlights()
            .stream()
            .map(code -> String.format("Cannot assign Flight %s", code))
            .forEach(System.out::println)
            ;

        System.out.printf("%d flights assigned \n", summary.getAssignedFlights());
    }

    private static String getRunwayStatusMessage(final String name, final boolean status) {
        return String.format("Runway %s is %s", name, status ? "open" : "close");
    }

    private static void validateRunway(final String runway) {

        if(runway == null || runway.isBlank()) {
            throw new IllegalArgumentException("'runway' argument must be provided");
        }
    } 

    private static FlightRunwayCategory getCategory(final String categoryName) {
        
        final String errorMessage =  
            "'category' argument must be provided. It should be one of the following values: " 
            + Arrays.stream(FlightRunwayCategory.values()).map(FlightRunwayCategory::toString).collect(Collectors.joining(", "))
            ;

        if(categoryName == null) {
            throw new IllegalArgumentException(errorMessage);
        }

        final FlightRunwayCategory category;

        try {
            category = FlightRunwayCategory.valueOf(categoryName);
        } catch(final IllegalArgumentException e) {
            throw new IllegalArgumentException(errorMessage);
        }

        return category;
    }

    private static Action getAction(final String actionName) {

        final String errorMessage = 
            "'action' argument must be provided. It should be one of the following values: " 
            + Arrays.stream(Action.values()).map(Action::getName).collect(Collectors.joining(", "))
            ;

        if(actionName == null) {
            throw new IllegalArgumentException(errorMessage);
        }

        final Action action;
        try {
            action = Action.valueOf(actionName);
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException(errorMessage);
        }

        return action;
    }

    public enum Action {
        ADD     ("add",     FlightAdministrationClient::addRunway   ),
        OPEN    ("open",    FlightAdministrationClient::open        ),
        CLOSE   ("close",   FlightAdministrationClient::close       ),
        STATUS  ("status",  FlightAdministrationClient::status      ),
        TAKE_OFF("takeOff", FlightAdministrationClient::takeOff     ),
        REORDER ("reorder", FlightAdministrationClient::reorder     ),
        ;

        private final String                        name;
        private final FlightAdministrationAction    handler;

        Action(final String name, final FlightAdministrationAction handler) {
            this.name       = name;
            this.handler    = handler;
        }

        public String getName() {
            return name;
        }

        public FlightAdministrationAction getHandler() {
            return handler;
        }

        public void execute(final FlightAdministrationService service, final String runway, final String categoryName) throws RemoteException {
            handler.execute(service, name, runway, categoryName);
        }
    }

    @FunctionalInterface
    private interface FlightAdministrationAction {
        void execute(final FlightAdministrationService service, final String actionName, final String runway, final String categoryName) throws RemoteException;
    }
}
