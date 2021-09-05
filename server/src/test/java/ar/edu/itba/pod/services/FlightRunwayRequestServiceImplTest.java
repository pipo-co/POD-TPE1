package ar.edu.itba.pod.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.rmi.RemoteException;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import ar.edu.itba.pod.exceptions.UniqueFlightCodeConstraintException;
import ar.edu.itba.pod.exceptions.UnregistrableFlightException;
import ar.edu.itba.pod.interfaces.FlightRunwayRequestService;
import ar.edu.itba.pod.models.Flight;
import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.server.repositories.AwaitingFlightsRepository;
import ar.edu.itba.pod.server.repositories.FlightRunwayRepository;
import ar.edu.itba.pod.server.repositories.impls.InMemoryAwaitingFlightsRepository;
import ar.edu.itba.pod.server.repositories.impls.InMemoryFlightRunwayRepository;
import ar.edu.itba.pod.server.services.FlightRunwayRequestServiceImpl;

public class FlightRunwayRequestServiceImplTest {

    private final AwaitingFlightsRepository awaitingFlightsRepository = InMemoryAwaitingFlightsRepository.getInstance();
    private final FlightRunwayRepository flightRunwayRepository = InMemoryFlightRunwayRepository.getInstance();

    final FlightRunwayRequestService trackRunwayRequestService = new FlightRunwayRequestServiceImpl(
            flightRunwayRepository, awaitingFlightsRepository);

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
    void registerFlightWithoutRunway() throws RemoteException {

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
    void registerFlightWithoutSuitableCategoryRunway() throws RemoteException {

        flightRunwayRepository.createRunway("R1", FlightRunwayCategory.A);
        assertThrows(UnregistrableFlightException.class,
                () -> trackRunwayRequestService.registerFlight("flight1", "COR2", "Latam", FlightRunwayCategory.F));

    }

    @Test
    void registerMultipleFlights() throws RemoteException, InterruptedException {

        flightRunwayRepository.createRunway("R1", FlightRunwayCategory.A);
        flightRunwayRepository.createRunway("R2", FlightRunwayCategory.B);
        flightRunwayRepository.createRunway("R3", FlightRunwayCategory.F);

        Runnable registerFlight = new Runnable() {
            @Override
            public void run() {

            }
        };

        Thread[] threads = new Thread[] {
                new Thread(() -> registerFlight("F1", "COR", "Latam", FlightRunwayCategory.A)),
                new Thread(() -> registerFlight("F2", "COR", "Latam", FlightRunwayCategory.C)),
                new Thread(() -> registerFlight("F3", "COR", "Latam", FlightRunwayCategory.D)),
                new Thread(() -> registerFlight("F4", "COR", "Latam", FlightRunwayCategory.E)),
                new Thread(() -> registerFlight("F5", "COR", "Latam", FlightRunwayCategory.F)), };

        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

        checkFlight("F1");
        checkFlight("F2");
        checkFlight("F3");
        checkFlight("F4");
        checkFlight("F5");

    }

    @Test
    void registerMultipleFlights2() throws RemoteException, InterruptedException {

        // pool.submit(Executors.callable(flightCreator));

        // pool.shutdown();
        // pool.awaitTermination(1000, TimeUnit.SECONDS);

        Thread[] threads = new Thread[] { new Thread(flightCreator1), new Thread(flightCreator2),
                new Thread(flightCreator3)};

        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

        for (int i = 0; i < 1000; i++) {
            checkFlight("flightCode1" + String.valueOf(i));
        }
        for (int i = 0; i < 1000; i++) {
            checkFlight("flightCode2" + String.valueOf(i));
        }
        for (int i = 0; i < 1000; i++) {
            checkFlight("flightCode3" + String.valueOf(i));
        }

    }

    private void registerFlight(String code, String destiny, String airline, FlightRunwayCategory category) {
        try {
            trackRunwayRequestService.registerFlight(code, destiny, airline, category);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private void checkFlight(String code) {

        Optional<Flight> flightOptional = awaitingFlightsRepository.getFlight(code);

        assertTrue(flightOptional.isPresent());
        assertEquals(code, awaitingFlightsRepository.getFlight(code).get().getCode());
    }

    private final Runnable flightCreator1 = () -> {
        for (int i = 0; i < 1000; i++) {
            registerFlight("flightCode1:" + String.valueOf(i), "COR", "Aerolineas Argentinas", FlightRunwayCategory.F);
        }
    };

    private final Runnable flightCreator2 = () -> {
        for (int i = 0; i < 1000; i++) {
            registerFlight("flightCode2" + String.valueOf(i), "COR", "Aerolineas Argentinas", FlightRunwayCategory.F);
        }
    };

    private final Runnable flightCreator3 = () -> {
        for (int i = 0; i < 1000; i++) {
            registerFlight("flightCode3" + String.valueOf(i), "COR", "Aerolineas Argentinas", FlightRunwayCategory.F);
        }
    };
}
