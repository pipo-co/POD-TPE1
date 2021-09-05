package ar.edu.itba.pod.server.repositories.impls;

import ar.edu.itba.pod.callbacks.FlightRunwayEventConsumer;
import ar.edu.itba.pod.exceptions.FlightNotFoundException;
import ar.edu.itba.pod.exceptions.UniqueFlightCodeConstraintException;
import ar.edu.itba.pod.models.Flight;
import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.server.models.InMemoryFlight;
import ar.edu.itba.pod.server.repositories.AwaitingFlightsRepository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryAwaitingFlightsRepository implements AwaitingFlightsRepository {

    private static final InMemoryAwaitingFlightsRepository instance = new InMemoryAwaitingFlightsRepository();
    public static InMemoryAwaitingFlightsRepository getInstance() {
        return instance;
    }

    private InMemoryAwaitingFlightsRepository() {
        // Singleton
    }

    private static final ConcurrentMap<String, InMemoryFlight> flights = new ConcurrentHashMap<>();

    @Override
    public Flight createFlight(final String code, final String airline, final String destination, final FlightRunwayCategory minCategory, final long orderRegisteredOn) throws UniqueFlightCodeConstraintException{
        final InMemoryFlight flight = new InMemoryFlight(code, airline, destination, minCategory, orderRegisteredOn);

        if(flights.putIfAbsent(flight.getCode(), flight) != null) {
            throw new UniqueFlightCodeConstraintException();
        }

        return flight;
    }

    @Override
    public Optional<Flight> getFlight(final String flight) {
        return Optional.ofNullable(flights.get(flight));
    }

    @Override
    public void deleteFlight(final String flight) {
        flights.remove(flight);
    }

    @Override
    public void addSubscriptionToFlight(final String flightCode, final FlightRunwayEventConsumer callback) throws FlightNotFoundException {
        final InMemoryFlight flight = flights.get(flightCode);
        if(flight == null) {
            throw new FlightNotFoundException();
        }

        flight.suscribeToRunwayEvent(callback);
    }
}
