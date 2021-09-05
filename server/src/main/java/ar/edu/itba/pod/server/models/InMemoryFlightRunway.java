package ar.edu.itba.pod.server.models;

import static ar.edu.itba.pod.models.FlightRunwayEvent.EventType.FLIGHT_TAKE_OFF;
import static ar.edu.itba.pod.models.FlightRunwayEvent.EventType.RUNWAY_ASSIGNMENT;
import static ar.edu.itba.pod.models.FlightRunwayEvent.EventType.RUNWAY_PROGRESS;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;

import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.models.FlightRunwayEvent;
import ar.edu.itba.pod.models.FlightTakeOff;
import ar.edu.itba.pod.models.Flight;
import ar.edu.itba.pod.models.FlightRunway;

public final class InMemoryFlightRunway implements FlightRunway {
    private final String                name;
    private final FlightRunwayCategory  category;
    private final Queue<InMemoryFlight> queuedFlights;
    private       boolean               open;           // ¡No acceder directamente! Sincronizado

    private final Object                queueLock;
    private final StampedLock           openLock;

    public InMemoryFlightRunway(final String name, final FlightRunwayCategory category) {
        this.name           = name;
        this.category       = category;
        this.open           = true;
        this.queuedFlights  = new LinkedList<>();

        this.queueLock      = new Object();
        this.openLock       = new StampedLock();
    }

    public void registerFlight(final InMemoryFlight flight) {
        synchronized(queueLock) {
            final int position = queuedFlights.size();
            queuedFlights.add(flight);
            flight.publishRunwayEvent(new FlightRunwayEvent(RUNWAY_ASSIGNMENT, flight.getCode(), name, position));
        }
    }

    public FlightTakeOff orderTakeOff(final long currentTakeOffOrder) {
        if(!isOpen()) {
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
            departedFlight.publishRunwayEvent(new FlightRunwayEvent(FLIGHT_TAKE_OFF, departedFlight.getCode(), name, -1));
        }
        if(progressedFlights != null) {
            int flightPos = 0;
            for(final InMemoryFlight flight : progressedFlights) {
                flight.publishRunwayEvent(new FlightRunwayEvent(RUNWAY_PROGRESS, flight.getCode(), name, flightPos));
                flightPos++;
            }
        }

        return departedFlight == null ?
            null :
            departedFlight.toTakeOff(currentTakeOffOrder, name)
            ;
    }

    public void cleanRunway(final Consumer<InMemoryFlight> callback) {
        synchronized(queueLock) {
            if(callback != null) {
                queuedFlights.forEach(callback);
            }
            queuedFlights.clear();
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
        long stamp = openLock.tryOptimisticRead();
        boolean ret = open;

        if(!openLock.validate(stamp)) {
            stamp = openLock.readLock();
            try {
                ret = open;
            } finally {
                openLock.unlock(stamp);
            }
        }

        return ret;
    }
    public void setOpen(final boolean open) {
        final long stamp = openLock.writeLock();
        try {
            this.open = open;
        } finally {
            openLock.unlock(stamp);
        }
    }
    public void open() {
        setOpen(true);
    }
    public void close() {
        setOpen(false);
    }
}
