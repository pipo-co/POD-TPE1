package ar.edu.itba.pod.exceptions;

public class UniqueRunwayNameConstraintException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    private static final String MESSAGE_TEMPLATE = "A Runway with name '%s' is already registered";

    public UniqueRunwayNameConstraintException(final String runwayName) {
        super(String.format(MESSAGE_TEMPLATE, runwayName));
    }
}