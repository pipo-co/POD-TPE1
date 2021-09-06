package ar.edu.itba.pod.client.models;

import ar.edu.itba.pod.models.FlightRunwayCategory;

public class RunwayRequest {
    private final String                flightCode;
    private final String                destinyAirport;
    private final String                airlineName;
    private final FlightRunwayCategory  category;

    public RunwayRequest(final String flightCode, final String destinyAirport, final String airlineName, final FlightRunwayCategory category) {
        this.flightCode     = flightCode;
        this.destinyAirport = destinyAirport;
        this.airlineName    = airlineName;
        this.category       = category;
    }

    public static RunwayRequest fromCSV(final String csvLine) {
        final String[] fields = csvLine.split(";");
        return new RunwayRequest(fields[0], fields[1], fields[2], FlightRunwayCategory.valueOf(fields[3]));
    }

    public String getFlightCode() {
        return flightCode;
    }

    public String getDestinyAirport() {
        return destinyAirport;
    }

    public String getAirlineName() {
        return airlineName;
    }

    public FlightRunwayCategory getCategory() {
        return category;
    }
}
