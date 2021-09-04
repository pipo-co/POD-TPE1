package ar.edu.itba.pod.interfaces;

import ar.edu.itba.pod.models.User;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TestInterface extends Remote {

    void dumbMethod() throws RemoteException;

    int dumbMethodInt() throws RemoteException;

    void dumbMethod(final User user) throws RemoteException;
}
