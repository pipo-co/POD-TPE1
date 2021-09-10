package ar.edu.itba.pod.server.models;

import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.models.FlightTakeOff;

public interface Flight {

    String getAirline();

    String getCode();

    String getDestination();

    FlightRunwayCategory getMinCategory();

    long getOrderRegisteredOn();

    default FlightTakeOff toTakeOff(final long currentTakeOffOrder, final String runway) {
        return new FlightTakeOff(
            currentTakeOffOrder - getOrderRegisteredOn() - 1,
            runway,
            getCode(),
            getAirline(),
            getDestination()
        );
    }
}
