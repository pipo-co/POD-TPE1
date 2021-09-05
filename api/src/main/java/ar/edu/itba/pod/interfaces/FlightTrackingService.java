package ar.edu.itba.pod.interfaces;

import ar.edu.itba.pod.callbacks.FlightRunwayEventConsumer;
import ar.edu.itba.pod.exceptions.AirlineFlightMismatchException;
import ar.edu.itba.pod.exceptions.FlightNotFoundException;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FlightTrackingService extends Remote {
    String CANONICAL_NAME = "flight_tracking";

    void suscribeToFlight(final String airline, final String flightCode, final FlightRunwayEventConsumer callback) throws RemoteException, FlightNotFoundException, AirlineFlightMismatchException;
}
