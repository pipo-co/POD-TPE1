package ar.edu.itba.pod.exceptions;

public class FlightNotFoundException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_MESSAGE = "Flight Not Found";

    public FlightNotFoundException() {
        super(DEFAULT_MESSAGE);
    }
}
