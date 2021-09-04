package ar.edu.itba.pod.interfaces;

import ar.edu.itba.pod.models.FlightTakeOff;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface FlightInfoService extends Remote {
    String CANONICAL_NAME = "flight_info";

    List<FlightTakeOff> queryTakeOffs() throws RemoteException;

    List<FlightTakeOff> queryAirlineTakeOffs(final String airline) throws RemoteException;

    List<FlightTakeOff> queryRunwayTakeOffs(final String runway) throws RemoteException;
}
