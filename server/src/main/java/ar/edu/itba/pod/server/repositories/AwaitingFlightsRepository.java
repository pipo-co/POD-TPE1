package ar.edu.itba.pod.server.repositories;

import ar.edu.itba.pod.server.models.Flight;

public interface AwaitingFlightsRepository {

    void addFlight(final Flight flight);

    Flight getFlight(final String flight);

    void removeFlight(final String flight);

    boolean containsFlight(final String flight);
}
