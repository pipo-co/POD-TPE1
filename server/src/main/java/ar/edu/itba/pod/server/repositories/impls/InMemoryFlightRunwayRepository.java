package ar.edu.itba.pod.server.repositories.impls;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
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

public class InMemoryFlightRunwayRepository implements FlightRunwayRepository {

    private static final Comparator<FlightRunway> RUNWAY_COMPARATOR = Comparator
        .comparing(FlightRunway::getCategory)
        .thenComparing(FlightRunway::getName)
        ;
    private static final Comparator<FlightRunway> EMPTIER_RUNWAY_COMPARATOR = Comparator
        .comparing(FlightRunway::awaitingFlights)
        .thenComparing(RUNWAY_COMPARATOR)
        ;

    private final Map<String, InMemoryFlightRunway>  runways;
    private final AtomicLong                         takeOffOrderCount;

    public InMemoryFlightRunwayRepository() {
        this.runways            = Collections.synchronizedMap(new HashMap<>());
        this.takeOffOrderCount  = new AtomicLong();
    }

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

    @Override
    public long getTakeOffOrderCount() {
        return takeOffOrderCount.get();
    }

    @Override
    public void orderTakeOff(final Consumer<FlightTakeOff> takeOffConsumer) {
        final List<FlightTakeOff> takeOffs;

        synchronized(runways) {
            final long currentOrderCount = takeOffOrderCount.incrementAndGet();

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
    public long reorderRunways(final Consumer<Flight> unregistrableConsumer) {

        final long[] totalReorderedFlights = {0}; 
        final List<InMemoryFlight> flights = new LinkedList<>();
        
        final Consumer<InMemoryFlight> flightConsumer = flights::add;
        final Consumer<Flight> subtracUnregistrableFlightsConsumer = f -> totalReorderedFlights[0]--;
        final Consumer<Flight> wrappedUnregistrableConsumer = subtracUnregistrableFlightsConsumer.andThen(unregistrableConsumer);

        boolean flag = true;
        InMemoryFlight auxFlight;
        final List<InMemoryFlightRunway> orderedRunways;
        synchronized(runways) {
            orderedRunways = runways.values()
                            .stream()
                            .sorted(RUNWAY_COMPARATOR)
                            .collect(Collectors.toList())
                            ;
                
            while(flag) {
                flag = false;
                for (InMemoryFlightRunway runway: orderedRunways) {
                    auxFlight = runway.popFlight();
                    if (auxFlight != null) {
                        flights.add(auxFlight);
                    }
                    if (runway.awaitingFlights() > 0) {
                        flag = true;
                    }
                }
            }
            
            totalReorderedFlights[0] = flights.size();

            flights.forEach(flight -> registerFlight(flight, wrappedUnregistrableConsumer));
        }

        return totalReorderedFlights[0];
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
