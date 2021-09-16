package ar.edu.itba.pod.exceptions;

public class UniqueFlightCodeConstraintException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    private static final String MESSAGE_TEMPLATE = "A flight with code '%s' is already registered";

    public UniqueFlightCodeConstraintException(final String flightCode) {
        super(String.format(MESSAGE_TEMPLATE, flightCode));
    }
}
