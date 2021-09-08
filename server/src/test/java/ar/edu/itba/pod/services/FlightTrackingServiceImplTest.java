package ar.edu.itba.pod.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.edu.itba.pod.callbacks.FlightRunwayEventConsumer;
import ar.edu.itba.pod.exceptions.AirlineFlightMismatchException;
import ar.edu.itba.pod.exceptions.FlightNotFoundException;
import ar.edu.itba.pod.interfaces.FlightRunwayRequestService;
import ar.edu.itba.pod.models.Flight;
import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.models.FlightRunwayEvent;
import ar.edu.itba.pod.models.FlightTakeOff;
import ar.edu.itba.pod.server.repositories.AwaitingFlightsRepository;
import ar.edu.itba.pod.server.repositories.FlightRunwayRepository;
import ar.edu.itba.pod.server.repositories.FlightTakeOffRepository;
import ar.edu.itba.pod.server.repositories.impls.InMemoryAwaitingFlightsRepository;
import ar.edu.itba.pod.server.repositories.impls.InMemoryFlightRunwayRepository;
import ar.edu.itba.pod.server.repositories.impls.InMemoryFlightTakeOffRepository;
import ar.edu.itba.pod.server.services.FlightAdministrationServiceImpl;
import ar.edu.itba.pod.server.services.FlightRunwayRequestServiceImpl;
import ar.edu.itba.pod.server.services.FlightTrackingServiceImpl;

public class FlightTrackingServiceImplTest {

    private FlightRunwayRepository flightRunwayRepository;
    private AwaitingFlightsRepository awaitingFlightsRepository;
    private FlightTakeOffRepository flightTakeOffRepository;
    
    private FlightRunwayRequestService flightRunwayRequestService;
    private FlightTrackingServiceImpl flightTrackingService;
    private FlightAdministrationServiceImpl flightAdministrationService;
    private List<String> runwayProgress;
    private List<String> runwayAssignment;
    private List<String> takeoffs;

    private FlightRunwayEventConsumer flightRunwayEventConsumer = flightEvent -> {
        if (flightEvent.getType() == FlightRunwayEvent.EventType.FLIGHT_TAKE_OFF) {
            takeoffs.add("Flight takeoff " + flightEvent.getFlight());
        }
        else if (flightEvent.getType() == FlightRunwayEvent.EventType.RUNWAY_ASSIGNMENT) {
            runwayAssignment.add("Flight " + flightEvent.getFlight() + " was reasigned to runway: " + flightEvent.getRunway());
        }
        else {
            runwayProgress.add("Flight " + flightEvent.getFlight() + " progressed to position: " + flightEvent.getPosition());
        }
    };

    @BeforeEach
    private void beforeEach() {

        this.flightRunwayRepository = new InMemoryFlightRunwayRepository();
        this.awaitingFlightsRepository = new InMemoryAwaitingFlightsRepository();
        this.flightTakeOffRepository = new InMemoryFlightTakeOffRepository();

        this.runwayProgress = new LinkedList<>();
        this.runwayAssignment = new LinkedList<>();
        this.takeoffs = new LinkedList<>();
        
        this.flightAdministrationService = new FlightAdministrationServiceImpl(flightRunwayRepository, flightTakeOffRepository, awaitingFlightsRepository);
        this.flightRunwayRequestService = new FlightRunwayRequestServiceImpl(flightRunwayRepository, awaitingFlightsRepository);
        this.flightTrackingService = new FlightTrackingServiceImpl(awaitingFlightsRepository);
    }

    @Test
    public void tracknonExistentFlight(){
        assertThrows(FlightNotFoundException.class, () -> flightTrackingService.suscribeToFlight("Aerolineas", "Flight1", flightRunwayEventConsumer));
    }

    @Test
    public void trackDifferentAirlineFlight(){

        flightRunwayRepository.createRunway("R1", FlightRunwayCategory.A);

        registerFlight("f1", "COR", "LAM", FlightRunwayCategory.A);
        
        assertThrows(AirlineFlightMismatchException.class, () -> flightTrackingService.suscribeToFlight("Aerolineas", "f1", flightRunwayEventConsumer));
    }

    @Test
    public void trackFlightRunwayProgressed() throws FlightNotFoundException, AirlineFlightMismatchException, RemoteException{

        flightRunwayRepository.createRunway("R1", FlightRunwayCategory.A);

        registerFlight("f1", "COR", "Aerolineas", FlightRunwayCategory.A);

        registerFlight("f2", "COR", "Aerolineas", FlightRunwayCategory.A);

        flightTrackingService.suscribeToFlight("Aerolineas", "f2", flightRunwayEventConsumer);

        flightAdministrationService.orderTakeOff();

        assertEquals("Flight f2 progressed to position: 0", runwayProgress.get(0));
        
    }

    @Test
    public void trackFlightRunwayAssignment() throws FlightNotFoundException, AirlineFlightMismatchException, RemoteException{

        flightRunwayRepository.createRunway("R1", FlightRunwayCategory.A);

        registerFlight("f1", "COR", "Aerolineas", FlightRunwayCategory.A);

        registerFlight("f2", "COR", "Aerolineas", FlightRunwayCategory.A);

        flightRunwayRepository.createRunway("R2", FlightRunwayCategory.A);

        flightTrackingService.suscribeToFlight("Aerolineas", "f2", flightRunwayEventConsumer);

        flightAdministrationService.reorderRunways();
        
        assertEquals("Flight f2 was reasigned to runway: R2", runwayAssignment.get(0));
        
    }

    @Test
    public void trackFlightOrderTakeoff() throws FlightNotFoundException, AirlineFlightMismatchException, RemoteException{

        flightRunwayRepository.createRunway("R1", FlightRunwayCategory.A);

        registerFlight("f1", "COR", "Aerolineas", FlightRunwayCategory.A);

        flightTrackingService.suscribeToFlight("Aerolineas", "f1", flightRunwayEventConsumer);

        flightAdministrationService.orderTakeOff();

        assertEquals("Flight takeoff f1", takeoffs.get(0));
        
    }
    

    private void checkFlight(String code) {

        Optional<Flight> flightOptional = awaitingFlightsRepository.getFlight(code);

        assertTrue(flightOptional.isPresent());
        assertEquals(code, flightOptional.get().getCode());
    }

    private void registerFlight(String code, String destiny, String airline, FlightRunwayCategory category) {
        try {
            flightRunwayRequestService.registerFlight(code, airline, destiny, category);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        checkFlight(code);
    }
}
