package ar.edu.itba.pod.server.models;

import ar.edu.itba.pod.callbacks.FlightRunwayEventConsumer;
import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.models.FlightRunwayEvent;
import ar.edu.itba.pod.models.FlightTakeOff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class Flight {
    private static final Logger logger = LoggerFactory.getLogger(Flight.class);

    private final String                            airline;
    private final String                            code;
    private final String                            destination;
    private final long                              orderRegisteredOn;
    private final FlightRunwayCategory              minCategory;
    private final List<FlightRunwayEventConsumer>   runwayEventSubscribers;

    public Flight(final String airline, final String code, final String destination, final FlightRunwayCategory minCategory, final long orderRegisteredOn) {
        this.airline                    = airline;
        this.code                       = code;
        this.destination                = destination;
        this.minCategory                = minCategory;
        this.orderRegisteredOn          = orderRegisteredOn;
        this.runwayEventSubscribers     = Collections.synchronizedList(new LinkedList<>());
    }

    public FlightTakeOff toTakeOff(final long orderTakeOff, final String runway) {

        return new FlightTakeOff(
            orderTakeOff - orderRegisteredOn - 1, 
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

    public String getAirline() {
        return airline;
    }

    public String getCode() {
        return code;
    }

    public String getDestination() {
        return destination;
    }

    public FlightRunwayCategory getMinCategory() {
        return minCategory;
    }

    public long getOrderRegisteredOn() {
        return orderRegisteredOn;
    }
}
