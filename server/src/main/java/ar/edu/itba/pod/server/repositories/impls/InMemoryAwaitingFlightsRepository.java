package ar.edu.itba.pod.server.repositories.impls;

import ar.edu.itba.pod.callbacks.FlightRunwayEventConsumer;
import ar.edu.itba.pod.exceptions.FlightNotFoundException;
import ar.edu.itba.pod.exceptions.UniqueFlightCodeConstraintException;
import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.server.models.Flight;
import ar.edu.itba.pod.server.repositories.AwaitingFlightsRepository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryAwaitingFlightsRepository implements AwaitingFlightsRepository {
    private final ConcurrentMap<String, InMemoryFlight> flights;

    public InMemoryAwaitingFlightsRepository() {
        this.flights = new ConcurrentHashMap<>();
    }

    @Override
    public Flight createFlight(final String code, final String airline, final String destination, final FlightRunwayCategory minCategory, final long orderRegisteredOn) throws UniqueFlightCodeConstraintException{
        final InMemoryFlight flight = new InMemoryFlight(code, airline, destination, minCategory, orderRegisteredOn);

        if(flights.putIfAbsent(flight.getCode(), flight) != null) {
            throw new UniqueFlightCodeConstraintException(flight.getCode());
        }

        return flight;
    }
    
    @Override
    public Optional<Flight> getFlight(final String code) {
        return Optional.ofNullable(flights.get(code));
    }

    @Override
    public int getAwaitingFlightsCount() {
        return flights.size();
    }

    @Override
    public void deleteFlight(final String code) {
        flights.remove(code);
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
