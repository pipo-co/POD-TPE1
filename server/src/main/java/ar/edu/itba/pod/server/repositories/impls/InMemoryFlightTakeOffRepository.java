package ar.edu.itba.pod.server.repositories.impls;

import ar.edu.itba.pod.models.FlightTakeOff;
import ar.edu.itba.pod.server.repositories.FlightTakeOffRepository;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public final class InMemoryFlightTakeOffRepository implements FlightTakeOffRepository {

    private static final InMemoryFlightTakeOffRepository instance = new InMemoryFlightTakeOffRepository();
    public static InMemoryFlightTakeOffRepository getInstance() {
        return instance;
    }

    private InMemoryFlightTakeOffRepository() {
        // Singleton
    }

    private static final List<FlightTakeOff> takeOffs = Collections.synchronizedList(new LinkedList<>());

    public void addTakeOff(final FlightTakeOff takeOff) {
        takeOffs.add(takeOff);
    }

    public void listTakeOffs(final Consumer<FlightTakeOff> callback) {
        synchronized(takeOffs) {
            takeOffs.forEach(callback);
        }
    }
}
