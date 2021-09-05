package ar.edu.itba.pod.server.services;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import ar.edu.itba.pod.exceptions.UniqueRunwayNameConstraintException;
import ar.edu.itba.pod.exceptions.RunwayNotFoundException;
import ar.edu.itba.pod.exceptions.UnregistrableFlightException;
import ar.edu.itba.pod.interfaces.FlightAdministrationService;
import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.models.Flight;
import ar.edu.itba.pod.models.FlightRunway;
import ar.edu.itba.pod.server.repositories.AwaitingFlightsRepository;
import ar.edu.itba.pod.server.repositories.FlightRunwayRepository;
import ar.edu.itba.pod.server.repositories.FlightTakeOffRepository;

import static java.util.Objects.*;

public class FlightAdministrationServiceImpl implements FlightAdministrationService {
    private final FlightRunwayRepository        flightRunwayRepository;
    private final FlightTakeOffRepository       flightTakeOffRepository;
    private final AwaitingFlightsRepository     awaitingFlightsRepository;

    public FlightAdministrationServiceImpl(
        final FlightRunwayRepository    flightRunwayRepository,
        final FlightTakeOffRepository   flightTakeOffRepository,
        final AwaitingFlightsRepository awaitingFlightsRepository
    ) {
        this.flightRunwayRepository     = requireNonNull(flightRunwayRepository);
        this.flightTakeOffRepository    = requireNonNull(flightTakeOffRepository);
        this.awaitingFlightsRepository  = requireNonNull(awaitingFlightsRepository);
    }

    @Override
    public void createRunway(final String name, final FlightRunwayCategory category) throws RemoteException, UniqueRunwayNameConstraintException {
        flightRunwayRepository.createRunway(name, category);
    }

    @Override
    public boolean isRunwayOpen(final String name) throws RemoteException, RunwayNotFoundException {
        return flightRunwayRepository
            .getRunway(name)
            .map(FlightRunway::isOpen)
            .orElseThrow(RunwayNotFoundException::new)
            ;
    }

    @Override
    public void openRunway(final String name) throws RemoteException, RunwayNotFoundException {
        flightRunwayRepository.openRunway(name);
    }

    @Override
    public void closeRunway(final String name) throws RemoteException, RunwayNotFoundException {
        flightRunwayRepository.closeRunway(name);
    }

    @Override
    public void orderTakeOff() throws RemoteException {
        flightRunwayRepository.orderTakeOff(flightTakeOff -> {
            awaitingFlightsRepository.deleteFlight(flightTakeOff.getFlight());
            flightTakeOffRepository.addTakeOff(flightTakeOff);
        });
    }

    @Override
    public void reorderRunways() throws RemoteException {
        final List<Flight> unregistrableFlights = new LinkedList<>();

        flightRunwayRepository.reorderRunways(unregistrableFlights::add);

        if(!unregistrableFlights.isEmpty()) {
            unregistrableFlights.forEach(flight -> awaitingFlightsRepository.deleteFlight(flight.getCode()));
            throw new UnregistrableFlightException(unregistrableFlights);
        }
    }
}
