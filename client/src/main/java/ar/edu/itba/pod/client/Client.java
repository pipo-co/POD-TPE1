package ar.edu.itba.pod.client;

import ar.edu.itba.pod.interfaces.TestInterface;
import ar.edu.itba.pod.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(final String[] args) throws RemoteException, NotBoundException {
        logger.info("tp1 Client Starting ...");
        final Registry registry = LocateRegistry.getRegistry();
        final TestInterface testRemote = (TestInterface) registry.lookup("test");
        testRemote.dumbMethod();
        testRemote.dumbMethod(new User("el_pepe"));
        logger.info("tp1 Client Ended");
    }
}
