package ar.edu.itba.pod.models;

import java.io.Serializable;

public final class FlightRunwayEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private final EventType type;
    private final String    flight;
    private final String    runway;
    private final int       position;

    public enum EventType {
        RUNWAY_ASSIGNMENT, RUNWAY_PROGRESS, FLIGHT_TAKE_OFF
    }

    public FlightRunwayEvent(final EventType type, final String flight, final String runway, final int position) {
        this.type       = type;
        this.flight     = flight;
        this.runway     = runway;
        this.position   = position;
    }

    public EventType getType() {
        return type;
    }
    public String getFlight() {
        return flight;
    }
    public String getRunway() {
        return runway;
    }
    public int getPosition() {
        return position;
    }
}
