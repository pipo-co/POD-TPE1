package ar.edu.itba.pod.server.models;

import ar.edu.itba.pod.models.FlightRunwayCategory;
import ar.edu.itba.pod.models.FlightTakeOff;

public interface Flight {

    FlightTakeOff toTakeOff(final long currentTakeOffOrder, final String runway);

    String getAirline();

    String getCode();

    String getDestination();

    FlightRunwayCategory getMinCategory();

    long getOrderRegisteredOn();
}
