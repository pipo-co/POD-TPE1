package ar.edu.itba.pod.services;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ar.edu.itba.pod.services.utils.CurrentThreadExecutorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.edu.itba.pod.exceptions.UniqueFlightCodeConstraintException;
import ar.edu.itba.pod.exceptions.UnregistrableFlightException;
import ar.edu.itba.pod.interfaces.FlightRunwayRequestService;
import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.server.models.Flight;
import ar.edu.itba.pod.server.repositories.AwaitingFlightsRepository;
import ar.edu.itba.pod.server.repositories.FlightRunwayRepository;
import ar.edu.itba.pod.server.repositories.impls.InMemoryAwaitingFlightsRepository;
import ar.edu.itba.pod.server.repositories.impls.InMemoryFlightRunwayRepository;
import ar.edu.itba.pod.server.services.FlightRunwayRequestServiceImpl;

import static org.junit.jupiter.api.Assertions.*;

public class FlightRunwayRequestServiceImplTest {

    private FlightRunwayRepository        flightRunwayRepository;
    private AwaitingFlightsRepository     awaitingFlightsRepository;
    private FlightRunwayRequestService    trackRunwayRequestService;

    @BeforeEach
    private void beforeEach() {
        this.flightRunwayRepository     = new InMemoryFlightRunwayRepository(new CurrentThreadExecutorService());
        this.awaitingFlightsRepository  = new InMemoryAwaitingFlightsRepository();
        this.trackRunwayRequestService  = new FlightRunwayRequestServiceImpl(
            flightRunwayRepository, awaitingFlightsRepository
        );
    }

    private final ExecutorService pool = Executors.newCachedThreadPool();

    @Test
    void registerFlight() throws RemoteException {

        flightRunwayRepository.createRunway("R1", FlightRunwayCategory.F);

        trackRunwayRequestService.registerFlight("flight1", "COR", "Aerolíneas Argentinas", FlightRunwayCategory.F);

        Optional<Flight> flightOptional = awaitingFlightsRepository.getFlight("flight1");

        assertTrue(flightOptional.isPresent());
        assertEquals("flight1", awaitingFlightsRepository.getFlight("flight1").get().getCode());

    }

    @Test
    void registerFlightWithoutRunway() {

        assertThrows(UnregistrableFlightException.class,
                () -> trackRunwayRequestService.registerFlight("flight1", "COR2", "Latam", FlightRunwayCategory.A));

    }

    @Test
    void registerDuplicatedFlights() throws RemoteException {

        flightRunwayRepository.createRunway("R1", FlightRunwayCategory.F);

        trackRunwayRequestService.registerFlight("flight1", "COR", "Aerolíneas Argentinas", FlightRunwayCategory.F);

        assertThrows(UniqueFlightCodeConstraintException.class,
                () -> trackRunwayRequestService.registerFlight("flight1", "COR2", "Latam", FlightRunwayCategory.A));

    }

    @Test
    void registerFlightWithoutSuitableCategoryRunway() {

        flightRunwayRepository.createRunway("R1", FlightRunwayCategory.A);
        assertThrows(UnregistrableFlightException.class,
                () -> trackRunwayRequestService.registerFlight("flight1", "COR2", "Latam", FlightRunwayCategory.F));
    }

    @Test
    void registerMultipleFlights() throws InterruptedException {

        flightRunwayRepository.createRunway("R1", FlightRunwayCategory.A);
        flightRunwayRepository.createRunway("R2", FlightRunwayCategory.B);
        flightRunwayRepository.createRunway("R3", FlightRunwayCategory.F);

        final Collection<Callable<Object>> callables = Stream
            .of(flightCreator1, flightCreator2, flightCreator3)
            .map(Executors::callable)
            .collect(Collectors.toList())
            ;

        pool.invokeAll(callables);
        pool.shutdown();
        if(!pool.awaitTermination(100, TimeUnit.SECONDS)) {
            fail("Threads no terminaron");
        }

        for (int i = 0; i < 1000; i++) {
            checkFlight("flightCode1" + i);
        }
        for (int i = 0; i < 1000; i++) {
            checkFlight("flightCode2" + i);
        }
        for (int i = 0; i < 1000; i++) {
            checkFlight("flightCode3" + i);
        }

        assertEquals(3000, flightRunwayRepository.getRunway("R3").get().awaitingFlights());
    }

    private void registerFlight(String code, String destiny, String airline, FlightRunwayCategory category) {
        try {
            trackRunwayRequestService.registerFlight(code, destiny, airline, category);
        } catch(final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkFlight(String code) {

        Optional<Flight> flightOptional = awaitingFlightsRepository.getFlight(code);

        assertTrue(flightOptional.isPresent());
        assertEquals(code, flightOptional.get().getCode());
    }

    private final Runnable flightCreator1 = () -> {
        for (int i = 0; i < 1000; i++) {
            registerFlight("flightCode1" + i, "COR", "Aerolineas Argentinas", FlightRunwayCategory.F);
        }
    };

    private final Runnable flightCreator2 = () -> {
        for (int i = 0; i < 1000; i++) {
            registerFlight("flightCode2" + i, "COR", "Aerolineas Argentinas", FlightRunwayCategory.F);
        }
    };

    private final Runnable flightCreator3 = () -> {
        for (int i = 0; i < 1000; i++) {
            registerFlight("flightCode3" + i, "COR", "Aerolineas Argentinas", FlightRunwayCategory.F);
        }
    };
}
