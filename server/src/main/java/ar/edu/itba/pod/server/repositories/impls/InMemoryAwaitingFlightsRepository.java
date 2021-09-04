package ar.edu.itba.pod.server.repositories.impls;

import ar.edu.itba.pod.server.models.Flight;
import ar.edu.itba.pod.server.repositories.AwaitingFlightsRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class InMemoryAwaitingFlightsRepository implements AwaitingFlightsRepository {

    private static final InMemoryAwaitingFlightsRepository instance = new InMemoryAwaitingFlightsRepository();
    public static InMemoryAwaitingFlightsRepository getInstance() {
        return instance;
    }

    private InMemoryAwaitingFlightsRepository() {
        // Singleton
    }

    private static final Map<String, Flight> flights = Collections.synchronizedMap(new HashMap<>());

    public void addFlight(final Flight flight) {
        flights.put(flight.getCode(), flight);
    }

    public Flight getFlight(final String flight) {
        return flights.get(flight);
    }

    public void removeFlight(final String flight) {
        flights.remove(flight);
    }
}
