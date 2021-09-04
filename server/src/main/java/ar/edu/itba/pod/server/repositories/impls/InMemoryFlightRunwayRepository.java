package ar.edu.itba.pod.server.repositories.impls;

import ar.edu.itba.pod.server.models.FlightRunway;
import ar.edu.itba.pod.server.repositories.FlightRunwayRepository;

import java.util.LinkedList;
import java.util.List;

public final class InMemoryFlightRunwayRepository implements FlightRunwayRepository {

    private static final InMemoryFlightRunwayRepository instance = new InMemoryFlightRunwayRepository();
    public static InMemoryFlightRunwayRepository getInstance() {
        return instance;
    }

    private InMemoryFlightRunwayRepository() {
        // Singleton
    }

    private static List<FlightRunway> runways = new LinkedList<>();


}
