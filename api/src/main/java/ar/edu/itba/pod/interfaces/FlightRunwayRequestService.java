package ar.edu.itba.pod.interfaces;

import ar.edu.itba.pod.models.FlightRunwayCategory;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FlightRunwayRequestService extends Remote {
    String CANONICAL_NAME = "flight_runway_request";

    void registerFlight(final String flight, final String airport, final String airline, final FlightRunwayCategory minCategory) throws RemoteException;
}
