package ar.edu.itba.pod.server.models;

import ar.edu.itba.pod.models.FlightRunwayCategory;

public class FlightRunway {
    private final String                name;
    private final FlightRunwayCategory  category;
    private boolean                     open;

    public FlightRunway(final String name, final FlightRunwayCategory category) {
        this.name       = name;
        this.category   = category;
        this.open       = true;
    }

    public String getName() {
        return name;
    }

    public FlightRunwayCategory getCategory() {
        return category;
    }

    public boolean isOpen() {
        return open;
    }
    public void open() {
        open = true;
    }
    public void close() {
        open = false;
    }
}
