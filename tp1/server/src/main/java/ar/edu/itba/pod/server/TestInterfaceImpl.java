package ar.edu.itba.pod.server;

import ar.edu.itba.pod.interfaces.TestInterface;
import ar.edu.itba.pod.models.User;

import java.rmi.RemoteException;

public class TestInterfaceImpl implements TestInterface {

    @Override
    public void dumbMethod() throws RemoteException {

    }

    @Override
    public int dumbMethodInt() throws RemoteException {
        return 0;
    }

    @Override
    public void dumbMethod(final User user) throws RemoteException {
        System.out.println(user);
    }
}
