package ar.edu.itba.pod.server.repositories.impls;

import static ar.edu.itba.pod.models.FlightRunwayEvent.EventType.FLIGHT_TAKE_OFF;
import static ar.edu.itba.pod.models.FlightRunwayEvent.EventType.RUNWAY_ASSIGNMENT;
import static ar.edu.itba.pod.models.FlightRunwayEvent.EventType.RUNWAY_PROGRESS;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.models.FlightRunwayEvent;
import ar.edu.itba.pod.models.FlightTakeOff;
import ar.edu.itba.pod.server.models.Flight;
import ar.edu.itba.pod.server.models.FlightRunway;

public final class InMemoryFlightRunway implements FlightRunway {
    private final String                name;
    private final FlightRunwayCategory  category;
    private final ExecutorService       executorService;
    private final Queue<InMemoryFlight> queuedFlights;
    private final AtomicBoolean         open;

    private final Object                queueLock;

    public InMemoryFlightRunway(final String name, final FlightRunwayCategory category, final ExecutorService executorService) {
        this.name               = name;
        this.category           = category;
        this.executorService    = executorService;
        this.queuedFlights      = new LinkedList<>();
        this.open               = new AtomicBoolean(true);

        this.queueLock          = new Object();
    }

    public void registerFlight(final InMemoryFlight flight) {
        synchronized(queueLock) {
            final int position = queuedFlights.size();
            queuedFlights.add(flight);
            flight.publishRunwayEvent(new FlightRunwayEvent(RUNWAY_ASSIGNMENT, flight.getCode(), name, flight.getDestination(), position), executorService);
        }
    }

    public FlightTakeOff orderTakeOff(final long currentTakeOffOrder) {
        if(!open.get()) {
            return null;
        }

        List<InMemoryFlight> progressedFlights = null;

        final InMemoryFlight departedFlight;
        synchronized(queueLock) {
            departedFlight = queuedFlights.poll();
            if(departedFlight != null) {
                progressedFlights = new LinkedList<>(queuedFlights);
            }
        }

        if(departedFlight != null) {
            departedFlight.publishRunwayEvent(
                new FlightRunwayEvent(FLIGHT_TAKE_OFF, departedFlight.getCode(), name, departedFlight.getDestination(), -1),
                executorService
            );
        }
        if(progressedFlights != null) {
            int flightPos = 0;
            for(final InMemoryFlight flight : progressedFlights) {
                flight.publishRunwayEvent(
                    new FlightRunwayEvent(RUNWAY_PROGRESS, flight.getCode(), name, departedFlight.getDestination(), flightPos),
                    executorService
                );
                flightPos++;
            }
        }

        return departedFlight == null ?
            null :
            departedFlight.toTakeOff(currentTakeOffOrder, name)
            ;
    }

    public InMemoryFlight pollFlight() {
        synchronized(queueLock) {
            return queuedFlights.poll();
        }
    }

    @Override
    public int awaitingFlights() {
        synchronized(queueLock) {
            return queuedFlights.size();
        }
    }

    @Override
    public void listAwaitingFlights(final Consumer<Flight> consumer) {
        synchronized(queueLock) {
            queuedFlights.forEach(consumer);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public FlightRunwayCategory getCategory() {
        return category;
    }

    @Override
    public boolean isOpen() {
        return open.get();
    }
    public void setOpen(final boolean open) {
        this.open.set(open);
    }
    public void open() {
        setOpen(true);
    }
    public void close() {
        setOpen(false);
    }
}
