package ar.edu.itba.pod.client.clients;

import static ar.edu.itba.pod.client.ClientUtils.DEFAULT_REGISTRY_ADDRESS;
import static ar.edu.itba.pod.client.ClientUtils.getRegistry;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
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
    private static final String REMOTE_EXCEPTION_MESSAGE = "Internal Error: Remote exception";
    private static Map<Action, Consumer<FlightAdministrationService>> actionHandlers = Map.of(
        Action.ADD,         FlightAdministrationClient::addRunway,
        Action.OPEN,        FlightAdministrationClient::open,
        Action.CLOSE,       FlightAdministrationClient::close,
        Action.STATUS,      FlightAdministrationClient::status,
        Action.TAKE_OFF,    FlightAdministrationClient::takeOff,
        Action.REORDER,     FlightAdministrationClient::reorder
    );


    public static void main(final String[] args) throws RemoteException, NotBoundException {
        logger.info("Flight Administration Client Started");

        final Registry registry = getRegistry(System.getProperty("serverAddress", DEFAULT_REGISTRY_ADDRESS));

        final FlightAdministrationService service = (FlightAdministrationService) registry.lookup(FlightAdministrationService.CANONICAL_NAME);

        try {
            actionHandlers.get(getAction()).accept(service);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        logger.info("Flight Administration Client Ended");
    }

    public static void addRunway(final FlightAdministrationService service) {

        final String runwayName = getRunwayName();
        final FlightRunwayCategory category = getCategory();

        final boolean status;
        try {
            status = service.createRunway(runwayName, category);
        } catch (UniqueRunwayNameConstraintException e) {
            throw new RuntimeException(String.format("There already exists a runway with the name %s.", runwayName), e);
        } catch (RemoteException e) {
            throw new RuntimeException(REMOTE_EXCEPTION_MESSAGE, e);
        }

        System.out.println(getRunwayStatusMessage(runwayName, status));
    }
    
    public static void open(final FlightAdministrationService service) {
        
        final String runwayName = getRunwayName();
        
        try {
            service.openRunway(runwayName);
        } catch (RunwayNotFoundException e) {
            throw new RuntimeException(String.format("Runway with name %s not found. Make sure to call 'open' with a existing runway.", runwayName), e);
        } catch (RemoteException e) {
            throw new RuntimeException(REMOTE_EXCEPTION_MESSAGE, e);
        }

        System.out.println(getRunwayStatusMessage(runwayName, true));
    }
    
    public static void close(final FlightAdministrationService service) {
        
        final String runwayName = getRunwayName();
        
        try {
            service.closeRunway(runwayName);
        } catch (RunwayNotFoundException e) {
            throw new RuntimeException(String.format("Runway with name %s not found. Make sure to call 'close' with a existing runway.", runwayName), e);
        } catch (RemoteException e) {
            throw new RuntimeException(REMOTE_EXCEPTION_MESSAGE, e);
        }

        System.out.println(getRunwayStatusMessage(runwayName, false));
    }
    
    public static void status(final FlightAdministrationService service) {
        
        final String runwayName = getRunwayName();
        
        final boolean status;
        try {
            status = service.isRunwayOpen(runwayName);
        } catch (RunwayNotFoundException e) {
            throw new RuntimeException(String.format("Runway with name %s not found. Make sure to call 'status' with a existing runway.", runwayName), e);
        } catch (RemoteException e) {
            throw new RuntimeException(REMOTE_EXCEPTION_MESSAGE, e);
        }

        System.out.println(getRunwayStatusMessage(runwayName, status));
    }
    
    public static void takeOff(final FlightAdministrationService service) {
        
        try {
            service.orderTakeOff();
        } catch (RemoteException e) {
            throw new RuntimeException(REMOTE_EXCEPTION_MESSAGE, e);
        }

        System.out.println("Flights departed!");
    }
    
    public static void reorder(final FlightAdministrationService service) {
        
        final RunwayReorderSummary summary;

        try {
            summary = service.reorderRunways();
        } catch (RemoteException e) {
            throw new RuntimeException(REMOTE_EXCEPTION_MESSAGE, e);
        }

        summary.getUnassignedFlights().stream().map(code -> String.format("Cannot assign Flight %s", code)).forEach(System.out::println);
        System.out.printf("%d flights assigned", summary.getAssignedFlights());
    }

    private static String getRunwayStatusMessage(final String name, final boolean status) {
        return String.format("Runway %s is %s", name, status ? "open" : "close");
    }

    private static String getRunwayName() {
        
        final String runway = System.getProperty("runway");

        if(runway == null || runway.isBlank()) {
            throw new IllegalArgumentException("'runway' argument must be provided");
        }

        return runway;
    } 

    private static FlightRunwayCategory getCategory() {
        
        final String errorMessage =  
            "'category' argument must be provided. It should be one of the following values: " 
            + Arrays.stream(FlightRunwayCategory.values()).map(FlightRunwayCategory::toString).collect(Collectors.joining(", "))
            ;

        final String categoryName = System.getProperty("category");

        if(categoryName == null) {
            throw new IllegalArgumentException(errorMessage);
        }

        FlightRunwayCategory category;

        try {
            category = FlightRunwayCategory.valueOf(categoryName); 
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(errorMessage);
        }

        return category;
    } 

    private static Action getAction() {

        final String actionName = System.getProperty("action");
        final String errorMessage = 
            "'action' argument must be provided. It should be one of the following values: " 
            + Arrays.stream(Action.values()).map(Action::getName).collect(Collectors.joining(", "))
            ;

        if(actionName == null) {
            throw new IllegalArgumentException(errorMessage);
        }

        final Action action = Action.fromName(actionName);

        if(action == null) {
            throw new IllegalArgumentException(errorMessage);
        }

        return action;
    }

    public enum Action {
        ADD("add"), 
        OPEN("open"), 
        CLOSE("close"), 
        STATUS("status"), 
        TAKE_OFF("takeOff"), 
        REORDER("reorder");

        private final String name;

        private Action(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Action fromName(final String name) {
            for (Action a : Action.values()) {
                if (a.name.equals(name)) {
                    return a;
                }
            }
            return null;
        }
    }
}
