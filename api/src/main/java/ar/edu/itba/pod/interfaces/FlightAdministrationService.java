package ar.edu.itba.pod.interfaces;

import ar.edu.itba.pod.models.FlightRunwayCategory;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FlightAdministrationService extends Remote {
    String CANONICAL_NAME = "flight_administration";

    void createRunway(final String name, final FlightRunwayCategory category) throws RemoteException;

    boolean isRunwayOpen(final String name) throws RemoteException;

    void openRunway(final String name) throws RemoteException;

    void closeRunway(final String name) throws RemoteException;

    void orderTakeOff() throws RemoteException;

    void reorderRunways() throws RemoteException;
}
