package ar.edu.itba.pod.exceptions;

import java.util.List;
import java.util.stream.Collectors;

import ar.edu.itba.pod.models.Flight;

public class UnregistrableFlightException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    final private List<String> flightCodes;

    public UnregistrableFlightException(final Flight flight) {
        flightCodes = List.of(flight.getCode());
    }

    public UnregistrableFlightException(final List<Flight> flights){
        flightCodes = flights
            .stream()
            .map(Flight::getCode)
            .collect(Collectors.toList())
            ;
    }

    @Override
    public String getMessage() {
        final StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("Unable to register flights: ");
        flightCodes.forEach(code -> errorMessage.append(code).append("; "));
        
        return errorMessage.toString();
    }
}