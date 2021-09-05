package ar.edu.itba.pod.server.repositories.impls;

import ar.edu.itba.pod.models.FlightTakeOff;
import ar.edu.itba.pod.server.repositories.FlightTakeOffRepository;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class InMemoryFlightTakeOffRepository implements FlightTakeOffRepository {

    private final List<FlightTakeOff> takeOffs;

    public InMemoryFlightTakeOffRepository() {
        this.takeOffs = Collections.synchronizedList(new LinkedList<>());
    }

    public void addTakeOff(final FlightTakeOff takeOff) {
        takeOffs.add(takeOff);
    }

    public void listTakeOffs(final Consumer<FlightTakeOff> callback) {
        
        final List<FlightTakeOff> takeOffsSnapshot;

        synchronized(takeOffs) {
            takeOffsSnapshot = new LinkedList<>(takeOffs);
        }

        takeOffsSnapshot.forEach(callback);
    }
}
