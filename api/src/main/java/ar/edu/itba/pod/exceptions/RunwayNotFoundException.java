package ar.edu.itba.pod.exceptions;

public class RunwayNotFoundException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_MESSAGE = "Runway Not Found";

    public RunwayNotFoundException() {
        super(DEFAULT_MESSAGE);
    }
}
