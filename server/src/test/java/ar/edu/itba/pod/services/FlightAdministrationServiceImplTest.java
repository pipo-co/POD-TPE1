package ar.edu.itba.pod.services;

import static org.junit.jupiter.api.Assertions.*;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.management.RuntimeErrorException;

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

    private FlightRunwayRepository flightRunwayRepository;
    private AwaitingFlightsRepository awaitingFlightsRepository;
    private FlightTakeOffRepository flightTakeOffRepository;
    private FlightRunwayRequestService flightRunwayRequestService;
    private FlightAdministrationService flightAdministrationService;

    private final ExecutorService pool = Executors.newCachedThreadPool();

    @BeforeEach
    private void beforeEach() {
        this.flightRunwayRepository = new InMemoryFlightRunwayRepository();
        this.awaitingFlightsRepository = new InMemoryAwaitingFlightsRepository();
        this.flightTakeOffRepository = new InMemoryFlightTakeOffRepository();
        this.flightRunwayRequestService = new FlightRunwayRequestServiceImpl(flightRunwayRepository,
                awaitingFlightsRepository);
        this.flightAdministrationService = new FlightAdministrationServiceImpl(flightRunwayRepository,
                flightTakeOffRepository, awaitingFlightsRepository);
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

        flightAdministrationService.orderTakeOff();

        assertFalse(awaitingFlightsRepository.getFlight("flightCode1").isPresent());
        assertFalse(awaitingFlightsRepository.getFlight("flightCode2").isPresent());
        assertFalse(awaitingFlightsRepository.getFlight("flightCode3").isPresent());

        assertEquals(1, flightRunwayRepository.getTakeOffOrderCount());

    }

    @Test
    void multipleTakeOffTests()
            throws UniqueFlightCodeConstraintException, UnregistrableFlightException, RemoteException {

        createMultipleRunways();
        registerMultipleFlightsSameRunway();

        registerOrderTakeOff("flightCode1", 1);

        assertTrue(awaitingFlightsRepository.getFlight("flightCode2").isPresent());

        registerOrderTakeOff("flightCode2", 2);

    }

    @Test
    void multipleThreadTakeOff() throws UniqueFlightCodeConstraintException, UnregistrableFlightException,
            RemoteException, InterruptedException {

        createMultipleRunways();

        final Collection<Callable<Object>> callables = Stream.of(flightCreator1, flightCreator2, flightCreator3)
                .map(Executors::callable).collect(Collectors.toList());

        pool.invokeAll(callables);
        pool.shutdown();

        if (!pool.awaitTermination(100, TimeUnit.SECONDS)) {
            fail("Threads no terminaron");
        }

        final ExecutorService secondPool = Executors.newCachedThreadPool();


        final Collection<Callable<Object>> secondCallables = Stream.of(orderTakeOffCounter, orderTakeOffCounter, orderTakeOffCounter, orderTakeOffCounter, orderTakeOffCounter)
                .map(Executors::callable).collect(Collectors.toList());

        secondPool.invokeAll(secondCallables);
        secondPool.shutdown();

        if (!secondPool.awaitTermination(100, TimeUnit.SECONDS)) {
            fail("Threads no terminaron");
        }
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

        flightRunwayRepository.getRunway("A4").get().listAwaitingFlights(flight -> flightListA4.add(flight));

        flightRunwayRepository.getRunway("R3").get().listAwaitingFlights(flight -> flightListR3.add(flight));

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

        flightRunwayRepository.getRunway("A4").get().listAwaitingFlights(flight -> flightListA42.add(flight));

        flightRunwayRepository.getRunway("R3").get().listAwaitingFlights(flight -> flightListR32.add(flight));

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
            throws UniqueFlightCodeConstraintException, UnregistrableFlightException, RemoteException {

        registerFlight("flightCode1", "airline", "destinationAirport", FlightRunwayCategory.A);
        registerFlight("flightCode2", "airline", "destinationAirport", FlightRunwayCategory.B);
        registerFlight("flightCode3", "airline", "destinationAirport", FlightRunwayCategory.F);

    }

    private void registerMultipleFlightsSameRunway()
            throws UniqueFlightCodeConstraintException, UnregistrableFlightException, RemoteException {

        registerFlight("flightCode1f", "airline", "destinationAirport", FlightRunwayCategory.F);
        registerFlight("flightCode2f", "airline", "destinationAirport", FlightRunwayCategory.F);

    }

    private void registerOrderTakeOff(String flightCode, long expectedCount) throws RemoteException {
        
        try {
            flightAdministrationService.orderTakeOff();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        assertFalse(awaitingFlightsRepository.getFlight(flightCode).isPresent());
        assertEquals(expectedCount, flightRunwayRepository.getTakeOffOrderCount());
    }

    private final Runnable flightCreator1 = () -> {
        for (int i = 0; i < 1000; i++) {
            registerFlight("flightCode1" + i, "COR", "Aerolineas Argentinas", FlightRunwayCategory.A);
        }
    };

    private final Runnable flightCreator2 = () -> {
        for (int i = 0; i < 1000; i++) {
            registerFlight("flightCode2" + i, "COR", "Aerolineas Argentinas", FlightRunwayCategory.B);
        }
    };

    private final Runnable flightCreator3 = () -> {
        for (int i = 0; i < 1000; i++) {
            registerFlight("flightCode3" + i, "COR", "Aerolineas Argentinas", FlightRunwayCategory.F);
        }
    };

    private final Runnable orderTakeOffCounter = () -> {
        long value;
        for (int i = 0; i < 200; i++) {
            value = flightRunwayRepository.getTakeOffOrderCount();
            try {
                registerOrderTakeOff("dummyFlight", value + 1);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };
}
