package ar.edu.itba.pod.server.services;

import java.rmi.RemoteException;

import ar.edu.itba.pod.interfaces.FlightAdministrationService;
import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.server.repositories.FlightRunwayRepository;
import ar.edu.itba.pod.server.repositories.impls.InMemoryFlightRunwayRepository;

public class FlightAdministrationServiceImpl implements FlightAdministrationService {

    
    private final FlightRunwayRepository flightRunwayRepository;

    public FlightAdministrationServiceImpl() {
        this.flightRunwayRepository = InMemoryFlightRunwayRepository.getInstance();
    }

    @Override
    public void createRunway(final String name, final FlightRunwayCategory category) throws RemoteException {
        
        if(!flightRunwayRepository.createRunway(name, category)) {
            throw new DuplicatedRunwayException();
        }
    }

    @Override
    public boolean isRunwayOpen(final String name) throws RemoteException {     
        return flightRunwayRepository.isRunwayOpen(name).orElseThrow(RunwayNotExistsException::new);
    }

    @Override
    public void openRunway(final String name) throws RemoteException {    
        
        if(!flightRunwayRepository.openRunway(name)) {
            throw new RunwayNotExistsException();
        }
    }

    @Override
    public void closeRunway(final String name) throws RemoteException {
        
        if(!flightRunwayRepository.closeRunway(name)) {
            throw new RunwayNotExistsException();
        }
    }

    @Override
    public void orderTakeOff() throws RemoteException {
    }

    @Override
    public void reorderRunways() throws RemoteException {
    }
}
