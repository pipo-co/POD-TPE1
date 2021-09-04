package ar.edu.itba.pod.server;

import ar.edu.itba.pod.interfaces.FlightAdministrationService;
import ar.edu.itba.pod.interfaces.FlightInfoService;
import ar.edu.itba.pod.interfaces.FlightRunwayRequestService;
import ar.edu.itba.pod.interfaces.FlightTrackingService;
import ar.edu.itba.pod.server.services.FlightAdministrationServiceImpl;
import ar.edu.itba.pod.server.services.FlightInfoServiceImpl;
import ar.edu.itba.pod.server.services.FlightRunwayRequestServiceImpl;
import ar.edu.itba.pod.server.services.FlightTrackingServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws RemoteException {
        logger.info("Server Started");

        final Registry registry = LocateRegistry.getRegistry();
        logger.info("Registry Found");

        final FlightAdministrationService adminService = new FlightAdministrationServiceImpl();
        registry.rebind(FlightAdministrationService.CANONICAL_NAME, UnicastRemoteObject.exportObject(adminService, 0));
        logger.info("Flight Administration Service Registered");

        final FlightInfoService infoService = new FlightInfoServiceImpl();
        registry.rebind(FlightInfoService.CANONICAL_NAME, UnicastRemoteObject.exportObject(infoService, 0));
        logger.info("Flight Information Service Registered");

        final FlightTrackingService trackingService = new FlightTrackingServiceImpl();
        registry.rebind(FlightTrackingService.CANONICAL_NAME, UnicastRemoteObject.exportObject(trackingService, 0));
        logger.info("Flight Tracking Service Registered");

        final FlightRunwayRequestService trackRequestService = new FlightRunwayRequestServiceImpl();
        registry.rebind(FlightRunwayRequestService.CANONICAL_NAME, UnicastRemoteObject.exportObject(trackRequestService, 0));
        logger.info("Flight Runway Request Service Registered");

        System.out.println("All Services Registered - Awaiting Requests ...");
    }
}
