package ar.edu.itba.pod.server.models;

import java.util.function.Consumer;

import ar.edu.itba.pod.models.FlightRunwayCategory;

public interface FlightRunway {

    int awaitingFlights();

    String getName();

    FlightRunwayCategory getCategory();

    boolean isOpen();

    void listAwaitingFlights(final Consumer<Flight> consumer);
}
