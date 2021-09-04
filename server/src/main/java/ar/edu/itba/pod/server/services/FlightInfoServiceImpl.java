package ar.edu.itba.pod.server.services;

import ar.edu.itba.pod.interfaces.FlightInfoService;
import ar.edu.itba.pod.models.FlightTakeOff;

import java.rmi.RemoteException;
import java.util.List;

public class FlightInfoServiceImpl implements FlightInfoService {

    @Override
    public List<FlightTakeOff> queryTakeOffs() throws RemoteException {
        return null;
    }

    @Override
    public List<FlightTakeOff> queryAirlineTakeOffs(final String airline) throws RemoteException {
        return null;
    }

    @Override
    public List<FlightTakeOff> queryRunwayTakeOffs(final String runway) throws RemoteException {
        return null;
    }
}
