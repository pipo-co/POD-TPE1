package ar.edu.itba.pod.models;

import java.util.function.Consumer;

public interface FlightRunway {

    int awaitingFlights();

    String getName();

    FlightRunwayCategory getCategory();

    boolean isOpen();

    void listAwaitingFlights(final Consumer<Flight> consumer);
}
