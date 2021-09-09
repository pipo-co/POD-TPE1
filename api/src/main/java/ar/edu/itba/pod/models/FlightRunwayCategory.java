package ar.edu.itba.pod.models;

import java.util.List;

public enum FlightRunwayCategory {
    A, B, C, D, E, F,
    ;
    public static final List<FlightRunwayCategory>  VALUES = List.of(values());
    public static final int                         SIZE = VALUES.size();
}
