package ar.edu.itba.pod.models;

import java.io.Serializable;
import java.util.List;

public class RunwayReorderSummary implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final long assignedFlights;
    private final List<String> unassignedFlightsCodes;
    
    public RunwayReorderSummary(final long assignedFlights, final List<String> unassignedFlightsCodes) {
        this.assignedFlights = assignedFlights;
        this.unassignedFlightsCodes = unassignedFlightsCodes;
    }

    public long getAssignedFlights() {
        return assignedFlights;
    }

    public List<String> getUnassignedFlights() {
        return unassignedFlightsCodes;
    }
}
