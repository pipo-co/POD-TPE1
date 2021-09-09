package ar.edu.itba.pod.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.edu.itba.pod.exceptions.UniqueFlightCodeConstraintException;
import ar.edu.itba.pod.exceptions.UniqueRunwayNameConstraintException;
import ar.edu.itba.pod.exceptions.UnregistrableFlightException;
import ar.edu.itba.pod.interfaces.FlightAdministrationService;
import ar.edu.itba.pod.interfaces.FlightRunwayRequestService;
import ar.edu.itba.pod.models.Flight;
import ar.edu.itba.pod.models.FlightRunway;
import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.server.repositories.AwaitingFlightsRepository;
import ar.edu.itba.pod.server.repositories.FlightRunwayRepository;
import ar.edu.itba.pod.server.repositories.FlightTakeOffRepository;
import ar.edu.itba.pod.server.repositories.impls.InMemoryAwaitingFlightsRepository;
import ar.edu.itba.pod.server.repositories.impls.InMemoryFlightRunwayRepository;
import ar.edu.itba.pod.server.repositories.impls.InMemoryFlightTakeOffRepository;
import ar.edu.itba.pod.server.services.FlightAdministrationServiceImpl;
import ar.edu.itba.pod.server.services.FlightRunwayRequestServiceImpl;

public class FlightAdministrationServiceImplTest {

    private FlightRunwayRepository      flightRunwayRepository;
    private AwaitingFlightsRepository   awaitingFlightsRepository;
    private FlightTakeOffRepository     flightTakeOffRepository;
    private FlightRunwayRequestService  flightRunwayRequestService;
    private FlightAdministrationService flightAdministrationService;

    private final ExecutorService pool = Executors.newCachedThreadPool();

    private static final int FLIGHTS_COUNT  = 1_000;
    private static final int THREAD_COUNT   = 20;

    @BeforeEach
    private void beforeEach() {
        this.flightRunwayRepository     = new InMemoryFlightRunwayRepository();
        this.awaitingFlightsRepository  = new InMemoryAwaitingFlightsRepository();
        this.flightTakeOffRepository    = new InMemoryFlightTakeOffRepository();

        this.flightRunwayRequestService = new FlightRunwayRequestServiceImpl(
            flightRunwayRepository, awaitingFlightsRepository
        );
        this.flightAdministrationService = new FlightAdministrationServiceImpl(
            flightRunwayRepository, flightTakeOffRepository, awaitingFlightsRepository
        );
    }

    @Test
    void runwayHandlingTest() throws UniqueRunwayNameConstraintException, RemoteException {

        assertTrue(flightAdministrationService.createRunway("R1", FlightRunwayCategory.F));

        assertTrue(flightAdministrationService.isRunwayOpen("R1"));

        flightAdministrationService.closeRunway("R1");

        assertFalse(flightAdministrationService.isRunwayOpen("R1"));

        assertThrows(UniqueRunwayNameConstraintException.class,
                () -> flightAdministrationService.createRunway("R1", FlightRunwayCategory.F));

    }

    @Test
    void takeOffTest() throws UniqueFlightCodeConstraintException, UnregistrableFlightException, RemoteException {
        createMultipleRunways();
        registerMultipleFlightsDifferentRunways();

        assertTrue(awaitingFlightsRepository.getFlight("flightCode1").isPresent());
        assertTrue(awaitingFlightsRepository.getFlight("flightCode2").isPresent());
        assertTrue(awaitingFlightsRepository.getFlight("flightCode3").isPresent());
        
        flightAdministrationService.orderTakeOff();

        assertFalse(awaitingFlightsRepository.getFlight("flightCode1").isPresent());
        assertFalse(awaitingFlightsRepository.getFlight("flightCode2").isPresent());
        assertFalse(awaitingFlightsRepository.getFlight("flightCode3").isPresent());

        assertEquals(1, flightRunwayRepository.getTakeOffOrderCount());
    }

    @Test
    void closedRunwayTakeOff() throws UniqueFlightCodeConstraintException, UnregistrableFlightException, RemoteException {
        
        createMultipleRunways();
        registerMultipleFlightsDifferentRunways();

        assertTrue(awaitingFlightsRepository.getFlight("flightCode1").isPresent());
        assertTrue(awaitingFlightsRepository.getFlight("flightCode2").isPresent());
        assertTrue(awaitingFlightsRepository.getFlight("flightCode3").isPresent());
        
        flightAdministrationService.closeRunway("R2");

        flightAdministrationService.orderTakeOff();

        assertFalse(awaitingFlightsRepository.getFlight("flightCode1").isPresent());
        assertFalse(awaitingFlightsRepository.getFlight("flightCode3").isPresent());
        
        assertTrue(awaitingFlightsRepository.getFlight("flightCode2").isPresent());
    }

    @Test
    void multipleTakeOffTests()
            throws UniqueFlightCodeConstraintException, UnregistrableFlightException {

        createMultipleRunways();
        registerMultipleFlightsSameRunway();

        registerOrderTakeOff("flightCode1f", 1);

        assertTrue(awaitingFlightsRepository.getFlight("flightCode2f").isPresent());
        
        registerOrderTakeOff("flightCode2f", 2);

        assertFalse(awaitingFlightsRepository.getFlight("flightCode2f").isPresent());
    }

    @Test
    void multipleThreadTakeOff() throws UniqueFlightCodeConstraintException, UnregistrableFlightException, InterruptedException {

        createMultipleRunways();

        final List<Callable<Object>> flightCreators = IntStream.range(0, THREAD_COUNT)
            .mapToObj(this::buildFlightCreatorRunnable)
            .map(Executors::callable)
            .collect(Collectors.toList())
            ;

        pool.invokeAll(flightCreators);

        assertEquals(THREAD_COUNT * FLIGHTS_COUNT, awaitingFlightsRepository.getAwaitingFlightsCount());

        final List<Callable<Object>> takeOffOrders = IntStream.range(0, 5 * THREAD_COUNT)
            .mapToObj(i -> buildOrderTakeOffCounter())
            .map(Executors::callable)
            .collect(Collectors.toList())
            ;

        pool.invokeAll(takeOffOrders);

        assertEquals(THREAD_COUNT * FLIGHTS_COUNT, flightRunwayRepository.getTakeOffOrderCount());
    }

    @Test
    void reorderTest() throws UniqueFlightCodeConstraintException, UnregistrableFlightException, RemoteException {

        createMultipleRunways(); 
        registerMultipleFlightsDifferentRunways();
        registerMultipleFlightsSameRunway();

        assertEquals(3, flightRunwayRepository.getRunway("R3").get().awaitingFlights());

        flightRunwayRepository.createRunway("A4", FlightRunwayCategory.F);

        flightAdministrationService.reorderRunways();
        
        List<Flight> flightListA4 = new LinkedList<>();
        List<Flight> flightListR3 = new LinkedList<>();

        flightRunwayRepository.getRunway("A4").get().listAwaitingFlights(flightListA4::add);

        flightRunwayRepository.getRunway("R3").get().listAwaitingFlights(flightListR3::add);

        assertFalse(flightListA4.isEmpty());

        System.out.println("vuelos A4 before reorder:");

        flightListA4.forEach(flight -> System.out.println(flight.getCode()));
        
        assertFalse(flightListR3.isEmpty());
        
        System.out.println("vuelos R3 before reorder:");

        flightListR3.forEach(flight -> System.out.println(flight.getCode()));

        registerFlight("f1", "CAN", "airline", FlightRunwayCategory.F);
        registerFlight("f2", "CAN", "airline", FlightRunwayCategory.F);
        registerFlight("f3", "CAN", "airline", FlightRunwayCategory.F);
        registerFlight("f4", "CAN", "airline", FlightRunwayCategory.F);
        registerFlight("f5", "CAN", "airline", FlightRunwayCategory.F);        
        registerFlight("f6", "CAN", "airline", FlightRunwayCategory.F);

        flightAdministrationService.reorderRunways();
        
        List<Flight> flightListA42 = new LinkedList<>();
        List<Flight> flightListR32 = new LinkedList<>();

        flightRunwayRepository.getRunway("A4").get().listAwaitingFlights(flightListA42::add);

        flightRunwayRepository.getRunway("R3").get().listAwaitingFlights(flightListR32::add);

        assertFalse(flightListA42.isEmpty());

        System.out.println("vuelos A4 after reorder:");

        flightListA42.forEach(flight -> System.out.println(flight.getCode()));
        
        assertFalse(flightListR32.isEmpty());
        
        System.out.println("vuelos R3 after reorder:");

        flightListR32.forEach(flight -> System.out.println(flight.getCode()));
    }

    @Test
    void reorderAfterClose() throws UniqueFlightCodeConstraintException, UnregistrableFlightException, RemoteException{
        
        createMultipleRunways(); 
        registerMultipleFlightsDifferentRunways();
        registerMultipleFlightsSameRunway();

        FlightRunway flightRunway = flightRunwayRepository.getRunway("R3").get();
        assertTrue(flightRunway.isOpen());
        assertEquals(3, flightRunway.awaitingFlights());

        flightAdministrationService.closeRunway("R3");
        
        assertFalse(flightRunway.isOpen());
        assertEquals(3, flightRunway.awaitingFlights());

        List<String> unassignedFlights = flightAdministrationService.reorderRunways().getUnassignedFlights();
        
        assertFalse(unassignedFlights.isEmpty());

        assertEquals("flightCode3", unassignedFlights.get(0));
        assertEquals("flightCode1f", unassignedFlights.get(1));
        assertEquals("flightCode2f", unassignedFlights.get(2));
        
    }

    private void createMultipleRunways() {
        flightRunwayRepository.createRunway("R1", FlightRunwayCategory.A);
        flightRunwayRepository.createRunway("R2", FlightRunwayCategory.B);
        flightRunwayRepository.createRunway("R3", FlightRunwayCategory.F);
    }

    private void checkFlight(String code) {

        Optional<Flight> flightOptional = awaitingFlightsRepository.getFlight(code);

        assertTrue(flightOptional.isPresent());
        assertEquals(code, flightOptional.get().getCode());
    }

    private void registerFlight(String code, String destiny, String airline, FlightRunwayCategory category) {
        try {
            flightRunwayRequestService.registerFlight(code, destiny, airline, category);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        checkFlight(code);
    }

    private void registerMultipleFlightsDifferentRunways()
            throws UniqueFlightCodeConstraintException, UnregistrableFlightException {

        registerFlight("flightCode1", "airline", "destinationAirport", FlightRunwayCategory.A);
        registerFlight("flightCode2", "airline", "destinationAirport", FlightRunwayCategory.B);
        registerFlight("flightCode3", "airline", "destinationAirport", FlightRunwayCategory.F);

    }

    private void registerMultipleFlightsSameRunway()
            throws UniqueFlightCodeConstraintException, UnregistrableFlightException {

        registerFlight("flightCode1f", "airline", "destinationAirport", FlightRunwayCategory.F);
        registerFlight("flightCode2f", "airline", "destinationAirport", FlightRunwayCategory.F);

    }

    private void registerOrderTakeOff(String flightCode, long expectedCount) {
        
        try {
            flightAdministrationService.orderTakeOff();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        assertFalse(awaitingFlightsRepository.getFlight(flightCode).isPresent());
        assertEquals(expectedCount, flightRunwayRepository.getTakeOffOrderCount());
    }

    private Runnable buildFlightCreatorRunnable(final int id) {
        return () -> {
            for(int i = 0; i < FLIGHTS_COUNT; i++) {
                registerFlight("flightCode-" + id + "-" + i, "COR", "airline", FlightRunwayCategory.VALUES.get(id % FlightRunwayCategory.SIZE));
            }
        };
    }

    private Runnable buildOrderTakeOffCounter() {
        return () -> {
            for (int i = 0; i < FLIGHTS_COUNT / 5; i++) {
                try {
                    flightAdministrationService.orderTakeOff();
                } catch (final RemoteException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
