package ar.edu.itba.pod.server.services;

import ar.edu.itba.pod.interfaces.FlightRunwayRequestService;
import ar.edu.itba.pod.models.FlightRunwayCategory;

import java.rmi.RemoteException;

public class FlightRunwayRequestServiceImpl implements FlightRunwayRequestService {

    @Override
    public void registerFlight(final String flight, final String airport, final String airline, final FlightRunwayCategory minCategory) throws RemoteException {

    }
}
