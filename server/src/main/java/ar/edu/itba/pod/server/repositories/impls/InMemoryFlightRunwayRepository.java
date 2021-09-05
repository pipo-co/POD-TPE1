package ar.edu.itba.pod.server.repositories.impls;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.models.FlightTakeOff;
import ar.edu.itba.pod.server.models.Flight;
import ar.edu.itba.pod.server.models.FlightRunway;
import ar.edu.itba.pod.server.repositories.FlightRunwayRepository;

public final class InMemoryFlightRunwayRepository implements FlightRunwayRepository {

    private static final InMemoryFlightRunwayRepository instance = new InMemoryFlightRunwayRepository();
    private static final Comparator<FlightRunway> selectRunwayComparator = Comparator
        .comparing(FlightRunway::awaitingFlights)
        .thenComparing(Comparator.comparing(FlightRunway::getCategory))
        .thenComparing(Comparator.comparing(FlightRunway::getName))
        ;

    public static InMemoryFlightRunwayRepository getInstance() {
        return instance;
    }

    private InMemoryFlightRunwayRepository() {
        // Singleton
    }

    private static Map<String, FlightRunway> runways = Collections.synchronizedMap(new HashMap<>());
    private static long takeOffOrderCount = 0L;
    private static StampedLock orderCountLock = new StampedLock();

    public boolean createRunway(final String name, final FlightRunwayCategory category) {

        if (runways.containsKey(name)) {
            return false;
        }

        runways.put(name, new FlightRunway(name, category));
        return true;
    }

    @Override
    public Optional<Boolean> isRunwayOpen(final String name) {
        return Optional.ofNullable(runways.get(name)).map(FlightRunway::isOpen);
    }

    @Override
    public boolean openRunway(final String name) {

        final FlightRunway runway = runways.get(name);

        if (runway == null) {
            return false;
        }

        runway.open();
        return true;
    }

    @Override
    public boolean closeRunway(final String name) {

        final FlightRunway runway = runways.get(name);

        if (runway == null) {
            return false;
        }

        runway.close();
        return true;
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

    public long getTakeOffOrderCount() {

        final long stamp = orderCountLock.tryOptimisticRead();
        final long value = takeOffOrderCount;

        if (!orderCountLock.validate(stamp)) {
            final long second_stamp = orderCountLock.readLock();
            final long second_value;

            try {
                second_value = takeOffOrderCount;
            } finally {
                orderCountLock.unlockRead(second_stamp); 
            }

            return second_value;
        }
        
        return value;
    }

    @Override
    public void orderTakeOff(final Consumer<FlightTakeOff> callback) {

        final List<FlightTakeOff> takeOffs;

        synchronized (runways) {

            final long currentOrderCount = incrementOrderCount();

            takeOffs = runways.values().stream()
                    .map(runway -> runway.orderTakeOff().toTakeOff(currentOrderCount, runway.getName()))
                    .collect(Collectors.toList());
        }

        takeOffs.forEach(callback);
    }

    @Override
    public void reorderRunways(final Consumer<Flight> unregistrableConsumer) {

        final List<Flight> flights = new LinkedList<>();

        synchronized (runways) {
            runways.values().stream()
                    .sorted(Comparator.comparing(FlightRunway::getCategory)
                            .thenComparing(Comparator.comparing(FlightRunway::getName)))
                    .forEach(runway -> runway.cleanRunway(flights::add));

            flights.forEach(flight -> registerFlight(flight, unregistrableConsumer)); // Que pasa si hay uno de estos vuelos que no se peude insertar (pistas cerradas) 
        }
    }

    @Override
    public boolean registerFlight(final Flight flight, final Consumer<Flight> unregistrableConsumer) {

        boolean[] inserted = { true };

        synchronized (runways) {

            runways.values().stream().filter(FlightRunway::isOpen)
                    .filter(runway -> runway.getCategory().compareTo(flight.getMinCategory()) >= 0)
                    .sorted(selectRunwayComparator)
                    .findFirst().ifPresentOrElse(runway -> runway.registerFlight(flight), () -> unregistrableConsumer.accept(flight));

        }

        return inserted[0];
    }
}
