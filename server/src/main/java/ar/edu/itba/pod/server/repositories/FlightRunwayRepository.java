package ar.edu.itba.pod.server.repositories;

import java.util.Optional;
import java.util.function.Consumer;

import ar.edu.itba.pod.exceptions.RunwayNotFoundException;
import ar.edu.itba.pod.exceptions.UniqueRunwayNameConstraintException;
import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.models.FlightTakeOff;
import ar.edu.itba.pod.models.Flight;
import ar.edu.itba.pod.models.FlightRunway;

public interface FlightRunwayRepository {

    FlightRunway createRunway(final String name, final FlightRunwayCategory category) throws UniqueRunwayNameConstraintException;

    Optional<FlightRunway> getRunway(final String name);

    void openRunway(final String name) throws RunwayNotFoundException;

    void closeRunway(final String name) throws RunwayNotFoundException;

    void orderTakeOff(final Consumer<FlightTakeOff> takeOffConsumer);

    long getTakeOffOrderCount();

    long reorderRunways(final Consumer<Flight> unregistrableConsumer);

    void registerFlight(final Flight flight, final Consumer<Flight> unregistrableConsumer);
}
