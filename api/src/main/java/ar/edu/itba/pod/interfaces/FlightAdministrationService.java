package ar.edu.itba.pod.interfaces;

import ar.edu.itba.pod.exceptions.RunwayNotFoundException;
import ar.edu.itba.pod.exceptions.UniqueFlightRunwayNameConstraintException;
import ar.edu.itba.pod.exceptions.UnregistrableFlightException;
import ar.edu.itba.pod.models.FlightRunwayCategory;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FlightAdministrationService extends Remote {
    String CANONICAL_NAME = "flight_administration";

    void createRunway(final String name, final FlightRunwayCategory category) throws RemoteException, UniqueFlightRunwayNameConstraintException;

    boolean isRunwayOpen(final String name) throws RemoteException, RunwayNotFoundException;

    void openRunway(final String name) throws RemoteException, RunwayNotFoundException;

    void closeRunway(final String name) throws RemoteException, RunwayNotFoundException;

    void orderTakeOff() throws RemoteException;

    void reorderRunways() throws RemoteException, UnregistrableFlightException;
}
