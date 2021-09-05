package ar.edu.itba.pod.server.repositories.impls;

import ar.edu.itba.pod.exceptions.UniqueFlightCodeConstraintException;
import ar.edu.itba.pod.models.Flight;
import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.server.models.InMemoryFlight;
import ar.edu.itba.pod.server.repositories.AwaitingFlightsRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryAwaitingFlightsRepository implements AwaitingFlightsRepository {

    private static final InMemoryAwaitingFlightsRepository instance = new InMemoryAwaitingFlightsRepository();
    public static InMemoryAwaitingFlightsRepository getInstance() {
        return instance;
    }

    private InMemoryAwaitingFlightsRepository() {
        // Singleton
    }

    private static final Map<String, Flight> flights = Collections.synchronizedMap(new HashMap<>());

    public Flight createFlight(final String code, final String airline, final String destination, final FlightRunwayCategory minCategory, final long orderRegisteredOn) throws UniqueFlightCodeConstraintException{
        final InMemoryFlight flight = new InMemoryFlight(code, airline, destination, minCategory, orderRegisteredOn);

        if(flights.putIfAbsent(flight.getCode(), flight) != null) {
            throw new UniqueFlightCodeConstraintException();
        }

        return flight;
    }
    
    public Optional<Flight> getFlight(final String flight) {
        return Optional.ofNullable(flights.get(flight));
    }

    public void deleteFlight(final String flight) {
        flights.remove(flight);
    }
}
