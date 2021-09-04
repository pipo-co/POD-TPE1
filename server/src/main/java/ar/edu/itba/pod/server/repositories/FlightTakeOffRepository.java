package ar.edu.itba.pod.server.repositories;

import ar.edu.itba.pod.models.FlightTakeOff;

import java.util.function.Consumer;

public interface FlightTakeOffRepository {

    void addTakeOff(final FlightTakeOff takeOff);

    void listTakeOffs(final Consumer<FlightTakeOff> callback);
}
