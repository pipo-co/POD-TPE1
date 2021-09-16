package ar.edu.itba.pod.exceptions;

public class UnregistrableFlightException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    private static final String MESSAGE_TEMPLATE = "Flight with code '%s' could not be registered to any runway";

    public UnregistrableFlightException(final String flightCode) {
        super(String.format(MESSAGE_TEMPLATE, flightCode));
    }

}