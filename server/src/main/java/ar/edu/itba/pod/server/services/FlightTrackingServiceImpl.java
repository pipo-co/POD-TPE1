package ar.edu.itba.pod.server.services;

import ar.edu.itba.pod.callbacks.FlightRunwayEventConsumer;
import ar.edu.itba.pod.exceptions.AirlineFlightMismatchException;
import ar.edu.itba.pod.exceptions.FlightNotFoundException;
import ar.edu.itba.pod.interfaces.FlightTrackingService;
import ar.edu.itba.pod.server.models.Flight;
import ar.edu.itba.pod.server.repositories.AwaitingFlightsRepository;

import java.rmi.RemoteException;

import static java.util.Objects.*;

public class FlightTrackingServiceImpl implements FlightTrackingService {

    private final AwaitingFlightsRepository awaitingFlightsRepository;

    public FlightTrackingServiceImpl(final AwaitingFlightsRepository awaitingFlightsRepository) {
        this.awaitingFlightsRepository = requireNonNull(awaitingFlightsRepository);
    }

    @Override
    public void suscribeToFlight(final String airline, final String flightCode, final FlightRunwayEventConsumer callback) throws RemoteException, FlightNotFoundException, AirlineFlightMismatchException {
        final Flight flight = awaitingFlightsRepository.getFlight(flightCode).orElseThrow(FlightNotFoundException::new);

        if(!flight.getAirline().equals(airline)) {
            throw new AirlineFlightMismatchException();
        }

        awaitingFlightsRepository.addSubscriptionToFlight(flight.getCode(), callback);
    }
}
