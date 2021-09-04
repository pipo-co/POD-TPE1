package ar.edu.itba.pod.server;

import ar.edu.itba.pod.interfaces.TestInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws RemoteException {
        logger.info("tp1 Server Starting ...");
        final TestInterface servant = new TestInterfaceImpl();
        final Remote remote = UnicastRemoteObject.exportObject(servant, 0);
        final Registry registry = LocateRegistry.getRegistry();
        registry.rebind("test", remote);
        System.out.println("Service bound");
    }
}
