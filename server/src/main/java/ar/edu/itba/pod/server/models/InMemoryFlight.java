package ar.edu.itba.pod.server.models;

import ar.edu.itba.pod.callbacks.FlightRunwayEventConsumer;
import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.models.FlightRunwayEvent;
import ar.edu.itba.pod.models.FlightTakeOff;

import ar.edu.itba.pod.models.Flight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class InMemoryFlight implements Flight {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryFlight.class);

    private final String                            airline;
    private final String                            code;
    private final String                            destination;
    private final long                              orderRegisteredOn;
    private final FlightRunwayCategory              minCategory;
    private final List<FlightRunwayEventConsumer>   runwayEventSubscribers;

    public InMemoryFlight(final String airline, final String code, final String destination, final FlightRunwayCategory minCategory, final long orderRegisteredOn) {
        this.airline                    = airline;
        this.code                       = code;
        this.destination                = destination;
        this.minCategory                = minCategory;
        this.orderRegisteredOn          = orderRegisteredOn;
        this.runwayEventSubscribers     = Collections.synchronizedList(new LinkedList<>());
    }

    @Override
    public FlightTakeOff toTakeOff(final long currentTakeOffOrder, final String runway) {
        return new FlightTakeOff(
            currentTakeOffOrder - orderRegisteredOn - 1,
            runway, 
            code, 
            airline,
            destination
        );
    }

    public void publishRunwayEvent(final FlightRunwayEvent event) {
        final List<Thread> callbackTasks = new ArrayList<>(runwayEventSubscribers.size());

        synchronized(runwayEventSubscribers) {
            for(final FlightRunwayEventConsumer subscriber : runwayEventSubscribers) {
                callbackTasks.add(new Thread(() -> {
                    try {
                        subscriber.accept(event);
                    } catch (final RemoteException e) {
                        logger.warn("Runway event callback on flight {} failed", code, e);
                    }
                }));
            }
        }

        callbackTasks.forEach(Thread::start);
    }

    public void suscribeToRunwayEvent(final FlightRunwayEventConsumer callback) {
        runwayEventSubscribers.add(callback);
    }

    @Override
    public String getAirline() {
        return airline;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDestination() {
        return destination;
    }

    @Override
    public FlightRunwayCategory getMinCategory() {
        return minCategory;
    }

    @Override
    public long getOrderRegisteredOn() {
        return orderRegisteredOn;
    }
}
