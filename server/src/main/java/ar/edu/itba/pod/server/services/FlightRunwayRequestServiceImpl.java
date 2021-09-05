package ar.edu.itba.pod.server.services;

import ar.edu.itba.pod.exceptions.UniqueFlightCodeConstraintException;
import ar.edu.itba.pod.exceptions.UnregistrableFlightException;
import ar.edu.itba.pod.interfaces.FlightRunwayRequestService;
import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.models.Flight;
import ar.edu.itba.pod.server.repositories.AwaitingFlightsRepository;
import ar.edu.itba.pod.server.repositories.FlightRunwayRepository;

import java.rmi.RemoteException;

import static java.util.Objects.*;

public class FlightRunwayRequestServiceImpl implements FlightRunwayRequestService {

    private final FlightRunwayRepository    flightRunwayRepository;
    private final AwaitingFlightsRepository awaitingFlightRepository;

    public FlightRunwayRequestServiceImpl(final FlightRunwayRepository flightRunwayRepository, final AwaitingFlightsRepository awaitingFlightRepository) {
        this.flightRunwayRepository     = requireNonNull(flightRunwayRepository);
        this.awaitingFlightRepository   = requireNonNull(awaitingFlightRepository);
    }

    @Override
    public void registerFlight(final String code, final String airline, final String destinationAirport, final FlightRunwayCategory minCategory) throws RemoteException, UniqueFlightCodeConstraintException {
        final Flight flight = awaitingFlightRepository.createFlight(code, airline, destinationAirport, minCategory, flightRunwayRepository.getTakeOffOrderCount());
        
        flightRunwayRepository.registerFlight(flight, UnregistrableFlightException::new);
    }
}
