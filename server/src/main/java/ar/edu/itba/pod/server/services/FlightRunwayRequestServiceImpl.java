package ar.edu.itba.pod.server.services;

import ar.edu.itba.pod.interfaces.FlightRunwayRequestService;
import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.server.models.Flight;
import ar.edu.itba.pod.server.repositories.AwaitingFlightsRepository;
import ar.edu.itba.pod.server.repositories.FlightRunwayRepository;
import ar.edu.itba.pod.server.repositories.impls.InMemoryAwaitingFlightsRepository;
import ar.edu.itba.pod.server.repositories.impls.InMemoryFlightRunwayRepository;

import java.rmi.RemoteException;

public class FlightRunwayRequestServiceImpl implements FlightRunwayRequestService {

    private final FlightRunwayRepository flightRunwayRepository;
    private final AwaitingFlightsRepository awaitingFlightRepository;

    public FlightRunwayRequestServiceImpl() {
        this.flightRunwayRepository = InMemoryFlightRunwayRepository.getInstance();
        this.awaitingFlightRepository = InMemoryAwaitingFlightsRepository.getInstance();
    }

    @Override
    public void registerFlight(final String flight, final String airport, final String airline, final FlightRunwayCategory minCategory) throws RemoteException {
        
        if (awaitingFlightRepository.containsFlight(flight)) {
            throw new DuplicatedFlightException();
        }

        Flight newFlight = new Flight(airline, flight, airport, minCategory, flightRunwayRepository.getTakeOffOrderCount());
        
        flightRunwayRepository.registerFlight(newFlight, UnregistrableFlightException::new);
        
        awaitingFlightRepository.addFlight(newFlight);

    }
}
