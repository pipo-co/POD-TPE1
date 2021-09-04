package ar.edu.itba.pod.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FlightAdministrationService extends Remote {
    String CANONICAL_NAME = "flight_administration";

    void dumbMethod() throws RemoteException;
}
