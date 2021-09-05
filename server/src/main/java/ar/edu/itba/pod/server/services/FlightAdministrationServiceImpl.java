package ar.edu.itba.pod.server.services;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import ar.edu.itba.pod.interfaces.FlightAdministrationService;
import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.server.models.Flight;
import ar.edu.itba.pod.server.repositories.FlightRunwayRepository;
import ar.edu.itba.pod.server.repositories.FlightTakeOffRepository;
import ar.edu.itba.pod.server.repositories.impls.InMemoryFlightRunwayRepository;
import ar.edu.itba.pod.server.repositories.impls.InMemoryFlightTakeOffRepository;

public class FlightAdministrationServiceImpl implements FlightAdministrationService {

    
    private final FlightRunwayRepository flightRunwayRepository;
    private final FlightTakeOffRepository flightTakeOffRepository;

    public FlightAdministrationServiceImpl() {
        this.flightRunwayRepository = InMemoryFlightRunwayRepository.getInstance();
        this.flightTakeOffRepository = InMemoryFlightTakeOffRepository.getInstance();
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

        flightRunwayRepository.orderTakeOff(flightTakeOffRepository::addTakeOff);
    }

    @Override
    public void reorderRunways() throws RemoteException {

        List<Flight> unregistrableFlights = new LinkedList<>();

        flightRunwayRepository.reorderRunways(unregistrableFlights::add);

        if (!unregistrableFlights.isEmpty()) {
            throw new UnregistrableFlightException(unregistrableFlights);
        }
    }
}
