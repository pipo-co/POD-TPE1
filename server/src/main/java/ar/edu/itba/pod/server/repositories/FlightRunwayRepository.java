package ar.edu.itba.pod.server.repositories;

import java.util.Optional;
import java.util.function.Consumer;

import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.models.FlightTakeOff;
import ar.edu.itba.pod.server.models.Flight;

public interface FlightRunwayRepository {

    boolean createRunway(final String name, final FlightRunwayCategory category);

    public Optional<Boolean> isRunwayOpen(final String name);

    /**
     * @return true if runwayway is present or else false
     */
    public boolean openRunway(final String name);

    /**
     * @return true if runwayway is present or else false
     */
    public boolean closeRunway(final String name);

    public void orderTakeOff(final Consumer<FlightTakeOff> callbackl, final Consumer<String> removeAwaitingFlight);

    public long getTakeOffOrderCount();

    public void reorderRunways(final Consumer<Flight> unregistrableConsumer);

    public void registerFlight(final Flight flight, final Consumer<Flight> unregistrableConsumer);
}
