package ar.edu.itba.pod.server.services;

import static java.util.Objects.requireNonNull;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import ar.edu.itba.pod.interfaces.FlightInfoService;
import ar.edu.itba.pod.models.FlightTakeOff;
import ar.edu.itba.pod.server.repositories.FlightTakeOffRepository;

public class FlightInfoServiceImpl implements FlightInfoService {

    private final FlightTakeOffRepository    flightTakeOffRepository;

    public FlightInfoServiceImpl(final FlightTakeOffRepository flightTakeOffRepository) {
        this.flightTakeOffRepository     = requireNonNull(flightTakeOffRepository);
    }

    private static final Predicate<FlightTakeOff> TRUE_PREDICATE = flightTakeOff -> true;

    @Override
    public List<FlightTakeOff> queryTakeOffs() throws RemoteException {
        return internalQueryTakeOffs(TRUE_PREDICATE);
    }

    @Override
    public List<FlightTakeOff> queryAirlineTakeOffs(final String airline) throws RemoteException {
        return internalQueryTakeOffs(to -> to.getAirline().equals(airline));
    }

    @Override
    public List<FlightTakeOff> queryRunwayTakeOffs(final String runway) throws RemoteException {
        return internalQueryTakeOffs(to -> to.getRunway().equals(runway));
    }

    private List<FlightTakeOff> internalQueryTakeOffs(final Predicate<FlightTakeOff> queryPredicate) {

        final List<FlightTakeOff> takeOffsSnapshot = new LinkedList<>();

        final Consumer<FlightTakeOff> processTakeOffs = 
            takeOff -> {
                if(queryPredicate.test(takeOff)) {
                    takeOffsSnapshot.add(takeOff); 
                }
            };

        flightTakeOffRepository.listTakeOffs(processTakeOffs);

        return takeOffsSnapshot;
    }

}
