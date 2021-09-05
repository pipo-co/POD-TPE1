package ar.edu.itba.pod.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import ar.edu.itba.pod.exceptions.RunwayNotFoundException;
import ar.edu.itba.pod.exceptions.UniqueRunwayNameConstraintException;
import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.models.RunwayReorderSummary;

public interface FlightAdministrationService extends Remote {
    String CANONICAL_NAME = "flight_administration";

    boolean createRunway(final String name, final FlightRunwayCategory category) throws RemoteException, UniqueRunwayNameConstraintException;

    boolean isRunwayOpen(final String name) throws RemoteException, RunwayNotFoundException;

    void openRunway(final String name) throws RemoteException, RunwayNotFoundException;

    void closeRunway(final String name) throws RemoteException, RunwayNotFoundException;

    void orderTakeOff() throws RemoteException;

    RunwayReorderSummary reorderRunways() throws RemoteException;
}
