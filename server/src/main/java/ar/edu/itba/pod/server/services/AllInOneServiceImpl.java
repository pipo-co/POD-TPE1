package ar.edu.itba.pod.server.services;

import ar.edu.itba.pod.callbacks.FlightRunwayEventConsumer;
import ar.edu.itba.pod.exceptions.AirlineFlightMismatchException;
import ar.edu.itba.pod.exceptions.FlightNotFoundException;
import ar.edu.itba.pod.exceptions.RunwayNotFoundException;
import ar.edu.itba.pod.exceptions.UniqueFlightCodeConstraintException;
import ar.edu.itba.pod.exceptions.UniqueRunwayNameConstraintException;
import ar.edu.itba.pod.exceptions.UnregistrableFlightException;
import ar.edu.itba.pod.interfaces.FlightAdministrationService;
import ar.edu.itba.pod.interfaces.FlightInfoService;
import ar.edu.itba.pod.interfaces.FlightRunwayRequestService;
import ar.edu.itba.pod.interfaces.FlightTrackingService;
import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.models.FlightTakeOff;
import ar.edu.itba.pod.models.RunwayReorderSummary;

import java.rmi.RemoteException;
import java.util.List;

public class AllInOneServiceImpl implements FlightAdministrationService, FlightInfoService, FlightRunwayRequestService, FlightTrackingService {

    private final FlightAdministrationService   administrationService;
    private final FlightInfoService             infoService;
    private final FlightRunwayRequestService    runwayRequestService;
    private final FlightTrackingService         trackingService;

    public AllInOneServiceImpl(final FlightAdministrationService administrationService, final FlightInfoService infoService, final FlightRunwayRequestService runwayRequestService, final FlightTrackingService trackingService) {
        this.administrationService  = administrationService;
        this.infoService            = infoService;
        this.runwayRequestService   = runwayRequestService;
        this.trackingService        = trackingService;
    }

    /* -------------------------------------- FlightAdministrationService ------------------------------------------ */

    @Override
    public boolean createRunway(final String name, final FlightRunwayCategory category) throws RemoteException, UniqueRunwayNameConstraintException {
        return administrationService.createRunway(name, category);
    }

    @Override
    public boolean isRunwayOpen(final String name) throws RemoteException, RunwayNotFoundException {
        return administrationService.isRunwayOpen(name);
    }

    @Override
    public void openRunway(final String name) throws RemoteException, RunwayNotFoundException {
        administrationService.openRunway(name);
    }

    @Override
    public void closeRunway(final String name) throws RemoteException, RunwayNotFoundException {
        administrationService.closeRunway(name);
    }

    @Override
    public void orderTakeOff() throws RemoteException {
        administrationService.orderTakeOff();
    }

    @Override
    public RunwayReorderSummary reorderRunways() throws RemoteException {
        return administrationService.reorderRunways();
    }

    /* ------------------------------------------ FlightInfoService ------------------------------------------------ */

    @Override
    public List<FlightTakeOff> queryTakeOffs() throws RemoteException {
        return infoService.queryTakeOffs();
    }

    @Override
    public List<FlightTakeOff> queryAirlineTakeOffs(final String airline) throws RemoteException {
        return infoService.queryAirlineTakeOffs(airline);
    }

    @Override
    public List<FlightTakeOff> queryRunwayTakeOffs(final String runway) throws RemoteException {
        return infoService.queryRunwayTakeOffs(runway);
    }

    /* --------------------------------------- FlightRunwayRequestService ------------------------------------------ */

    @Override
    public void registerFlight(final String code, final String airline, final String destinationAirport, final FlightRunwayCategory minCategory) throws RemoteException, UniqueFlightCodeConstraintException, UnregistrableFlightException {
        runwayRequestService.registerFlight(code, airline, destinationAirport, minCategory);
    }

    /* ------------------------------------------ FlightTrackingService -------------------------------------------- */

    @Override
    public void suscribeToFlight(final String airline, final String flightCode, final FlightRunwayEventConsumer callback) throws RemoteException, FlightNotFoundException, AirlineFlightMismatchException {
        trackingService.suscribeToFlight(airline, flightCode, callback);
    }
}
