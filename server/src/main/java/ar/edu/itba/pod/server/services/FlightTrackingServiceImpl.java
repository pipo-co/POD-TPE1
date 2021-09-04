package ar.edu.itba.pod.server.services;

import ar.edu.itba.pod.callbacks.FlightRunwayEventConsumer;
import ar.edu.itba.pod.interfaces.FlightTrackingService;

import java.rmi.RemoteException;

public class FlightTrackingServiceImpl implements FlightTrackingService {

    @Override
    public void suscribeToFlight(final String airline, final String flight, final FlightRunwayEventConsumer callback) throws RemoteException {

    }
}
