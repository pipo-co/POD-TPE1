package ar.edu.itba.pod.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import ar.edu.itba.pod.services.utils.CurrentThreadExecutorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.edu.itba.pod.exceptions.AirlineFlightMismatchException;
import ar.edu.itba.pod.exceptions.FlightNotFoundException;
import ar.edu.itba.pod.interfaces.FlightRunwayRequestService;
import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.models.FlightRunwayEvent;
import ar.edu.itba.pod.server.models.Flight;
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

    private FlightRunwayRepository          flightRunwayRepository;
    private AwaitingFlightsRepository       awaitingFlightsRepository;
    private FlightTakeOffRepository         flightTakeOffRepository;
    
    private FlightRunwayRequestService      flightRunwayRequestService;
    private FlightTrackingServiceImpl       flightTrackingService;
    private FlightAdministrationServiceImpl flightAdministrationService;

    @BeforeEach
    private void beforeEach() {

        this.flightRunwayRepository         = new InMemoryFlightRunwayRepository(new CurrentThreadExecutorService());
        this.awaitingFlightsRepository      = new InMemoryAwaitingFlightsRepository();
        this.flightTakeOffRepository        = new InMemoryFlightTakeOffRepository();
        
        this.flightAdministrationService    = new FlightAdministrationServiceImpl(flightRunwayRepository, flightTakeOffRepository, awaitingFlightsRepository);
        this.flightRunwayRequestService     = new FlightRunwayRequestServiceImpl(flightRunwayRepository, awaitingFlightsRepository);
        this.flightTrackingService          = new FlightTrackingServiceImpl(awaitingFlightsRepository);
    }

    @Test
    public void tracknonExistentFlight(){
        assertThrows(FlightNotFoundException.class, () -> flightTrackingService.suscribeToFlight("Aerolineas", "Flight1", e-> {}));
    }

    @Test
    public void trackDifferentAirlineFlight(){

        flightRunwayRepository.createRunway("R1", FlightRunwayCategory.A);

        registerFlight("f1", "COR", "LAM", FlightRunwayCategory.A);
        
        assertThrows(AirlineFlightMismatchException.class, () -> flightTrackingService.suscribeToFlight("Aerolineas", "f1", e -> {}));
    }

    @Test
    public void trackFlightRunwayProgressed() throws FlightNotFoundException, AirlineFlightMismatchException, RemoteException, InterruptedException{

        List<String> runwayProgress = new LinkedList<>();

        flightRunwayRepository.createRunway("R1", FlightRunwayCategory.A);

        registerFlight("f1", "COR", "Aerolineas", FlightRunwayCategory.A);

        registerFlight("f2", "COR", "Aerolineas", FlightRunwayCategory.A);

        flightTrackingService.suscribeToFlight("Aerolineas", "f2", e -> {
            if (e.getType() == FlightRunwayEvent.EventType.RUNWAY_PROGRESS) {
                runwayProgress.add("Flight " + e.getFlight() + " progressed to position: " + e.getPosition());
            }
        });

        flightAdministrationService.orderTakeOff();

        assertEquals("Flight f2 progressed to position: 0", runwayProgress.get(0));
        
    }

    @Test
    public void trackFlightRunwayAssignment() throws FlightNotFoundException, AirlineFlightMismatchException, RemoteException {

        List<String> runwayAssignment = new LinkedList<>();

        flightRunwayRepository.createRunway("R1", FlightRunwayCategory.A);

        registerFlight("f1", "COR", "Aerolineas", FlightRunwayCategory.A);

        registerFlight("f2", "COR", "Aerolineas", FlightRunwayCategory.A);

        flightRunwayRepository.createRunway("R2", FlightRunwayCategory.A);

        flightTrackingService.suscribeToFlight("Aerolineas", "f2", e -> {
            if (e.getType() == FlightRunwayEvent.EventType.RUNWAY_ASSIGNMENT) {
                runwayAssignment.add("Flight " + e.getFlight() + " was reasigned to runway: " + e.getRunway());
            }
        });
        
        flightAdministrationService.reorderRunways();
        
        assertEquals("Flight f2 was reasigned to runway: R2", runwayAssignment.get(0));
        
    }

    @Test
    public void trackFlightOrderTakeoff() throws FlightNotFoundException, AirlineFlightMismatchException, RemoteException, InterruptedException{

        List<String> takeoffs = new LinkedList<>();

        flightRunwayRepository.createRunway("R1", FlightRunwayCategory.A);

        registerFlight("f1", "COR", "Aerolineas", FlightRunwayCategory.A);

        flightTrackingService.suscribeToFlight("Aerolineas", "f1", e -> {
            if (e.getType() == FlightRunwayEvent.EventType.FLIGHT_TAKE_OFF) {
                takeoffs.add("Flight takeoff " + e.getFlight());
            }
        });
        
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
