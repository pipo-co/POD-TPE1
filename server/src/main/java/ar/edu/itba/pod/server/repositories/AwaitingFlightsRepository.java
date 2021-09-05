package ar.edu.itba.pod.server.repositories;

import ar.edu.itba.pod.callbacks.FlightRunwayEventConsumer;
import ar.edu.itba.pod.exceptions.FlightNotFoundException;
import ar.edu.itba.pod.exceptions.UniqueFlightCodeConstraintException;
import ar.edu.itba.pod.models.Flight;
import ar.edu.itba.pod.models.FlightRunwayCategory;

import java.util.Optional;

public interface AwaitingFlightsRepository {

    Flight createFlight(final String code, final String airline, final String destination, final FlightRunwayCategory minCategory, final long orderRegisteredOn) throws UniqueFlightCodeConstraintException;

    Optional<Flight> getFlight(final String flight);

    void deleteFlight(final String flight);

    void addSubscriptionToFlight(final String flightCode, final FlightRunwayEventConsumer callback) throws FlightNotFoundException;
}
