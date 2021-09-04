package ar.edu.itba.pod.interfaces;

import ar.edu.itba.pod.callbacks.FlightRunwayEventConsumer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FlightTrackingService extends Remote {
    String CANONICAL_NAME = "flight_tracking";

    void suscribeToFlight(final String airline, final String flight, final FlightRunwayEventConsumer callback) throws RemoteException;
}
