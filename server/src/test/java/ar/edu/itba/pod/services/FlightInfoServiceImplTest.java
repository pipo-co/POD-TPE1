package ar.edu.itba.pod.services;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.rmi.RemoteException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import ar.edu.itba.pod.services.utils.CurrentThreadExecutorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.edu.itba.pod.exceptions.UniqueFlightCodeConstraintException;
import ar.edu.itba.pod.exceptions.UnregistrableFlightException;
import ar.edu.itba.pod.interfaces.FlightRunwayRequestService;
import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.models.FlightTakeOff;
import ar.edu.itba.pod.server.repositories.AwaitingFlightsRepository;
import ar.edu.itba.pod.server.repositories.FlightRunwayRepository;
import ar.edu.itba.pod.server.repositories.FlightTakeOffRepository;
import ar.edu.itba.pod.server.repositories.impls.InMemoryAwaitingFlightsRepository;
import ar.edu.itba.pod.server.repositories.impls.InMemoryFlightRunwayRepository;
import ar.edu.itba.pod.server.repositories.impls.InMemoryFlightTakeOffRepository;
import ar.edu.itba.pod.server.services.FlightAdministrationServiceImpl;
import ar.edu.itba.pod.server.services.FlightInfoServiceImpl;
import ar.edu.itba.pod.server.services.FlightRunwayRequestServiceImpl;

public class FlightInfoServiceImplTest {
    
    private FlightRunwayRepository          flightRunwayRepository;
    private AwaitingFlightsRepository       awaitingFlightsRepository;
    private FlightTakeOffRepository         flightTakeOffRepository;
    
    private FlightRunwayRequestService      flightRunwayRequestService;
    private FlightInfoServiceImpl           flightInfoService;
    private FlightAdministrationServiceImpl flightAdministrationService;

    @BeforeEach
    private void beforeEach() {

        this.flightRunwayRepository         = new InMemoryFlightRunwayRepository(new CurrentThreadExecutorService());
        this.awaitingFlightsRepository      = new InMemoryAwaitingFlightsRepository();
        this.flightTakeOffRepository        = new InMemoryFlightTakeOffRepository();
        
        this.flightAdministrationService    = new FlightAdministrationServiceImpl(flightRunwayRepository, flightTakeOffRepository, awaitingFlightsRepository);
        this.flightRunwayRequestService     = new FlightRunwayRequestServiceImpl(flightRunwayRepository, awaitingFlightsRepository);
        this.flightInfoService              = new FlightInfoServiceImpl(flightTakeOffRepository);
    }

    @Test
    void testQueryAirlineTakeOffs() throws UniqueFlightCodeConstraintException, UnregistrableFlightException, RemoteException {
        flightRunwayRepository.createRunway("R1", FlightRunwayCategory.A);
        flightRunwayRepository.createRunway("R2", FlightRunwayCategory.B);
        flightRunwayRepository.createRunway("R3", FlightRunwayCategory.F);

        flightRunwayRequestService.registerFlight("flightCode1", "airline", "destinationAirport", FlightRunwayCategory.A);
        flightRunwayRequestService.registerFlight("flightCode2", "airline2", "destinationAirport", FlightRunwayCategory.B);
        flightRunwayRequestService.registerFlight("flightCode3", "airline2", "destinationAirport", FlightRunwayCategory.F);
        flightRunwayRequestService.registerFlight("flightCode4", "airline", "destinationAirport", FlightRunwayCategory.F);

        flightAdministrationService.orderTakeOff();

        final List<FlightTakeOff> takeoffs = flightInfoService.queryAirlineTakeOffs("airline2");
        final List<String> expected = List.of("flightCode2", "flightCode3");
        final List<String> notExpected = List.of("flightCode1", "flightCode4");

        assertTrue(takeoffs.stream().map(FlightTakeOff::getFlight).allMatch(expected::contains));
        assertTrue(expected.containsAll(takeoffs.stream().map(FlightTakeOff::getFlight).collect(Collectors.toList())));

        assertTrue(takeoffs.stream().map(FlightTakeOff::getFlight).allMatch(Predicate.not(notExpected::contains)));
    }

    @Test
    void testQueryRunwayTakeOffs() throws RemoteException {
        flightRunwayRepository.createRunway("R1", FlightRunwayCategory.A);
        flightRunwayRepository.createRunway("R2", FlightRunwayCategory.B);
        flightRunwayRepository.createRunway("R3", FlightRunwayCategory.F);

        flightRunwayRequestService.registerFlight("flightCode1", "airline", "destinationAirport", FlightRunwayCategory.A);
        flightRunwayRequestService.registerFlight("flightCode2", "airline", "destinationAirport", FlightRunwayCategory.B);
        flightRunwayRequestService.registerFlight("flightCode3", "airline", "destinationAirport", FlightRunwayCategory.F);
        flightRunwayRequestService.registerFlight("flightCode4", "airline", "destinationAirport", FlightRunwayCategory.F);

        flightAdministrationService.orderTakeOff();

        final List<FlightTakeOff> takeoffs = flightInfoService.queryRunwayTakeOffs("R1");
        final List<String> expected = List.of("flightCode1");
        final List<String> notExpected = List.of("flightCode2", "flightCode3", "flightCode4");

        assertTrue(takeoffs.stream().map(FlightTakeOff::getFlight).allMatch(expected::contains));
        assertTrue(expected.containsAll(takeoffs.stream().map(FlightTakeOff::getFlight).collect(Collectors.toList())));
        
        assertTrue(takeoffs.stream().map(FlightTakeOff::getFlight).allMatch(Predicate.not(notExpected::contains)));
    }
    
    @Test
    void testQueryTakeOffs() throws RemoteException {
        
        flightRunwayRepository.createRunway("R1", FlightRunwayCategory.A);
        flightRunwayRepository.createRunway("R2", FlightRunwayCategory.B);
        flightRunwayRepository.createRunway("R3", FlightRunwayCategory.F);
        
        flightRunwayRequestService.registerFlight("flightCode1", "airline", "destinationAirport", FlightRunwayCategory.A);
        flightRunwayRequestService.registerFlight("flightCode2", "airline", "destinationAirport", FlightRunwayCategory.B);
        flightRunwayRequestService.registerFlight("flightCode3", "airline", "destinationAirport", FlightRunwayCategory.F);
        flightRunwayRequestService.registerFlight("flightCode4", "airline", "destinationAirport", FlightRunwayCategory.F);
        
        flightAdministrationService.orderTakeOff();
        
        final List<FlightTakeOff> takeoffs = flightInfoService.queryTakeOffs();
        final List<String> expected = List.of("flightCode1", "flightCode2", "flightCode3");
        final List<String> notExpected = List.of("flightCode4");

        assertTrue(takeoffs.stream().map(FlightTakeOff::getFlight).allMatch(expected::contains));
        assertTrue(expected.containsAll(takeoffs.stream().map(FlightTakeOff::getFlight).collect(Collectors.toList())));
        
        assertTrue(takeoffs.stream().map(FlightTakeOff::getFlight).allMatch(Predicate.not(notExpected::contains)));
    }
}
