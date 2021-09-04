package ar.edu.itba.pod.models;

import java.io.Serializable;

public final class FlightTakeOff implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long      ordersWaited;
    private final String    runway;
    private final String    flight;
    private final String    airline;
    private final String    destinyAirport;

    public FlightTakeOff(final long ordersWaited, final String runway, final String flight, final String airline, final String destinyAirport) {
        this.ordersWaited   = ordersWaited;
        this.runway         = runway;
        this.flight         = flight;
        this.airline        = airline;
        this.destinyAirport = destinyAirport;
    }

    public long getOrdersWaited() {
        return ordersWaited;
    }
    public String getRunway() {
        return runway;
    }
    public String getFlight() {
        return flight;
    }
    public String getAirline() {
        return airline;
    }
    public String getDestinyAirport() {
        return destinyAirport;
    }
}
