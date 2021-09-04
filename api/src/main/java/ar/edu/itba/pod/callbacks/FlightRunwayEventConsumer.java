package ar.edu.itba.pod.callbacks;

import ar.edu.itba.pod.models.FlightRunwayEvent;

import java.rmi.Remote;
import java.rmi.RemoteException;

@FunctionalInterface
public interface FlightRunwayEventConsumer extends Remote {

    void accept(final FlightRunwayEvent event) throws RemoteException;
}
