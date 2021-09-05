package ar.edu.itba.pod.server.services;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import ar.edu.itba.pod.server.models.Flight;

public class UnregistrableFlightException extends IllegalStateException {

    final private List<String> flightCodes;

    public UnregistrableFlightException(Flight flight) {
        flightCodes = List.of(flight.getCode());
    }

    public UnregistrableFlightException(List<Flight> flights){
        flightCodes = flights.stream().map(flight -> flight.getCode()).collect(Collectors.toList());
    }

    @Override
    public String getMessage() {
        final StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("Unable to register flights: ");
        flightCodes.forEach(code -> errorMessage.append(code + "; "));
        
        return errorMessage.toString();
    }
}