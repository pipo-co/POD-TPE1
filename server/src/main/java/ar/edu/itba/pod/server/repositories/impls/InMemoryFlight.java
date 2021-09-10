package ar.edu.itba.pod.server.repositories.impls;

import ar.edu.itba.pod.callbacks.FlightRunwayEventConsumer;
import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.models.FlightRunwayEvent;
import ar.edu.itba.pod.server.models.Flight;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public final class InMemoryFlight implements Flight {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryFlight.class);

    private final String                            airline;
    private final String                            code;
    private final String                            destination;
    private final long                              orderRegisteredOn;
    private final FlightRunwayCategory              minCategory;
    private final List<FlightRunwayEventConsumer>   runwayEventSubscribers;

    public InMemoryFlight(final String code, final String airline, final String destination, final FlightRunwayCategory minCategory, final long orderRegisteredOn) {
        this.code                       = code;
        this.airline                    = airline;
        this.destination                = destination;
        this.minCategory                = minCategory;
        this.orderRegisteredOn          = orderRegisteredOn;
        this.runwayEventSubscribers     = Collections.synchronizedList(new LinkedList<>());
    }

    public void publishRunwayEvent(final FlightRunwayEvent event, final ExecutorService executorService) {
        final List<Runnable> callbackTasks = new ArrayList<>(runwayEventSubscribers.size());

        synchronized(runwayEventSubscribers) {
            for(final FlightRunwayEventConsumer subscriber : runwayEventSubscribers) {
                callbackTasks.add(() -> {
                    try {
                        subscriber.accept(event);
                    } catch(final RemoteException e) {
                        logger.warn("Runway event callback on flight {} failed", code, e);
                    }
                });
            }
        }

        callbackTasks.forEach(executorService::submit);
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
