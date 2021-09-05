package ar.edu.itba.pod.models;

public interface Flight {

    FlightTakeOff toTakeOff(final long currentTakeOffOrder, final String runway);

    String getAirline();

    String getCode();

    String getDestination();

    FlightRunwayCategory getMinCategory();

    long getOrderRegisteredOn();
}
