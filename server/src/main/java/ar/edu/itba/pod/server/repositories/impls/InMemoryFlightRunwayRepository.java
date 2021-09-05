package ar.edu.itba.pod.server.repositories.impls;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import ar.edu.itba.pod.exceptions.RunwayNotFoundException;
import ar.edu.itba.pod.exceptions.UniqueRunwayNameConstraintException;
import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.models.FlightTakeOff;
import ar.edu.itba.pod.server.exceptions.UnsupportedFlightException;
import ar.edu.itba.pod.models.Flight;
import ar.edu.itba.pod.models.FlightRunway;
import ar.edu.itba.pod.server.models.InMemoryFlight;
import ar.edu.itba.pod.server.models.InMemoryFlightRunway;
import ar.edu.itba.pod.server.repositories.FlightRunwayRepository;

public final class InMemoryFlightRunwayRepository implements FlightRunwayRepository {

    private static final InMemoryFlightRunwayRepository instance = new InMemoryFlightRunwayRepository();

    public static InMemoryFlightRunwayRepository getInstance() {
        return instance;
    }

    private InMemoryFlightRunwayRepository() {
        // Singleton
    }

    private static final Comparator<FlightRunway> RUNWAY_COMPARATOR = Comparator
        .comparing(FlightRunway::getCategory)
        .thenComparing(FlightRunway::getName)
        ;
    private static final Comparator<FlightRunway> EMPTIER_RUNWAY_COMPARATOR = Comparator
        .comparing(FlightRunway::awaitingFlights)
        .thenComparing(RUNWAY_COMPARATOR)
        ;

    private static final Map<String, InMemoryFlightRunway>  runways             = Collections.synchronizedMap(new HashMap<>());
    private static       long                               takeOffOrderCount   = 0L;
    private static final StampedLock                        orderCountLock      = new StampedLock();

    @Override
    public FlightRunway createRunway(final String name, final FlightRunwayCategory category) throws UniqueRunwayNameConstraintException {
        final InMemoryFlightRunway runway = new InMemoryFlightRunway(name, category);

        if(runways.putIfAbsent(name, runway) != null) {
            throw new UniqueRunwayNameConstraintException();
        }

        return runway;
    }

    @Override
    public Optional<FlightRunway> getRunway(final String name) {
        return Optional.ofNullable(runways.get(name));
    }

    @Override
    public void openRunway(final String name) throws RunwayNotFoundException {
        final InMemoryFlightRunway runway = runways.get(name);
        if(runway == null) {
            throw new RunwayNotFoundException();
        }

        runway.open();
    }

    @Override
    public void closeRunway(final String name) throws RunwayNotFoundException {
        final InMemoryFlightRunway runway = runways.get(name);
        if(runway == null) {
            throw new RunwayNotFoundException();
        }

        runway.close();
    }

    private long incrementOrderCount() {
        final long newValue;

        final long stamp = orderCountLock.writeLock();
        try {
            takeOffOrderCount++;
            newValue = takeOffOrderCount;
        } finally {
            orderCountLock.unlockWrite(stamp);
        }

        return newValue;
    }

    @Override
    public long getTakeOffOrderCount() {
        long stamp = orderCountLock.tryOptimisticRead();
        long ret = takeOffOrderCount;

        if(!orderCountLock.validate(stamp)) {

            stamp = orderCountLock.readLock();
            try {
                ret = takeOffOrderCount;
            } finally {
                orderCountLock.unlockRead(stamp);
            }
        }

        return ret;
    }

    @Override
    public void orderTakeOff(final Consumer<FlightTakeOff> takeOffConsumer) {
        final List<FlightTakeOff> takeOffs;

        synchronized(runways) {
            final long currentOrderCount = incrementOrderCount();

            takeOffs = runways.values()
                .stream()
                .map(runway -> runway.orderTakeOff(currentOrderCount))
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
                ;
        }

        takeOffs.forEach(takeOffConsumer);
    }

    @Override
    public void reorderRunways(final Consumer<Flight> unregistrableConsumer) {

        final List<InMemoryFlight> flights = new LinkedList<>();
        final Consumer<InMemoryFlight> flightConsumer = flights::add;

        synchronized(runways) {
            runways.values()
                .stream()
                .sorted(RUNWAY_COMPARATOR)
                .forEach(runway -> runway.cleanRunway(flightConsumer))
                ;

            flights.forEach(flight -> registerFlight(flight, unregistrableConsumer));
        }
    }

    private static final Runnable EMPTY_RUNNABLE = () -> {};

    private void registerFlight(final InMemoryFlight flight, final Consumer<Flight> unregistrableConsumer) {
        final Runnable unregistrableRunnable = unregistrableConsumer == null ?
            EMPTY_RUNNABLE :
            () -> unregistrableConsumer.accept(flight)
            ;

        synchronized(runways) {
            runways.values()
                .stream()
                .filter(FlightRunway::isOpen)
                .filter(runway -> runway.getCategory().compareTo(flight.getMinCategory()) >= 0)
                .min(EMPTIER_RUNWAY_COMPARATOR)
                .ifPresentOrElse(runway -> runway.registerFlight(flight), unregistrableRunnable)
            ;
        }
    }

    @Override
    public void registerFlight(final Flight flight, final Consumer<Flight> unregistrableConsumer) throws UnsupportedFlightException {
        if(!(flight instanceof InMemoryFlight)) {
            // Solo aceptamos flights de este tipo
            throw new UnsupportedFlightException();
        }
        registerFlight((InMemoryFlight) flight, unregistrableConsumer);
    }
}
