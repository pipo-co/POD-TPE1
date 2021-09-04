package ar.edu.itba.pod.server.services;

import ar.edu.itba.pod.interfaces.FlightAdministrationService;
import ar.edu.itba.pod.models.FlightRunwayCategory;

import java.rmi.RemoteException;

public class FlightAdministrationServiceImpl implements FlightAdministrationService {

    @Override
    public void createRunway(final String name, final FlightRunwayCategory category) throws RemoteException {

    }

    @Override
    public boolean isRunwayOpen(final String runway) throws RemoteException {
        return false;
    }

    @Override
    public void openRunway(final String runway) throws RemoteException {

    }

    @Override
    public void closeRunway(final String runway) throws RemoteException {

    }

    @Override
    public void orderTakeOff() throws RemoteException {

    }

    @Override
    public void reorderRunways() throws RemoteException {

    }
}
