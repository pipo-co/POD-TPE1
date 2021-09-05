package ar.edu.itba.pod.server.models;

import static ar.edu.itba.pod.models.FlightRunwayEvent.EventType.FLIGHT_TAKE_OFF;
import static ar.edu.itba.pod.models.FlightRunwayEvent.EventType.RUNWAY_ASSIGNMENT;
import static ar.edu.itba.pod.models.FlightRunwayEvent.EventType.RUNWAY_PROGRESS;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.models.FlightRunwayEvent;

public class FlightRunway {
    private final String                name;
    private final FlightRunwayCategory  category;
    private final Queue<Flight>         queuedFlights;
    private boolean                     open;           // ¡No acceder directamente! Sincronizado

    private final Object                queueLock;
    private final ReadWriteLock         openLock;       // TODO(tobi): Mejor lock?
    private final Lock                  openReadLock;
    private final Lock                  openWriteLock;

    public FlightRunway(final String name, final FlightRunwayCategory category) {
        this.name           = name;
        this.category       = category;
        this.open           = true;
        this.queuedFlights  = new LinkedList<>();

        this.queueLock      = new Object();
        this.openLock       = new ReentrantReadWriteLock();
        this.openReadLock   = this.openLock.readLock();
        this.openWriteLock  = this.openLock.writeLock();
    }

    public void registerFlight(final Flight flight) {
        synchronized(queueLock) {
            final int position = queuedFlights.size();
            queuedFlights.add(flight);
            flight.publishRunwayEvent(new FlightRunwayEvent(RUNWAY_ASSIGNMENT, flight.getCode(), name, position));
        }
    }

    public Flight orderTakeOff(final Consumer<String> removeAwaitingFlight) {
        if(!isOpen()) {
            return null;
        }

        List<Flight> progressedFlights = null;

        final Flight departedFlight;
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
            for(final Flight flight : progressedFlights) {
                flight.publishRunwayEvent(new FlightRunwayEvent(RUNWAY_PROGRESS, flight.getCode(), name, flightPos));
                flightPos++;
            }
        }

        removeAwaitingFlight.accept(departedFlight.getCode());

        return departedFlight;
    }

    public void cleanRunway(final Consumer<Flight> callback) {
        synchronized(queueLock) {
            if(callback != null) {
                queuedFlights.forEach(callback);
            }
            queuedFlights.clear();
        }
    }

    public int awaitingFlights() {
        synchronized(queueLock) {
            return queuedFlights.size();
        }
    }

    public String getName() {
        return name;
    }

    public FlightRunwayCategory getCategory() {
        return category;
    }

    public boolean isOpen() {
        final boolean ret;
        openReadLock.lock();
        try {
            ret = open;
        } finally {
            openReadLock.unlock();
        }
        return ret;
    }
    public void setOpen(final boolean open) {
        openWriteLock.lock();
        try {
            this.open = open;
        } finally {
            openWriteLock.unlock();
        }
    }
    public void open() {
        setOpen(true);
    }
    public void close() {
        setOpen(false);
    }
}
