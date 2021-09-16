package ar.edu.itba.pod.exceptions;

public class AirlineFlightMismatchException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    private static final String MESSAGE_TEMPLATE = "Airline '%s' did not match flight with code '%s'";

    public AirlineFlightMismatchException(final String airline, final String flightCode) {
        super(String.format(MESSAGE_TEMPLATE, airline, flightCode));
    }
}
