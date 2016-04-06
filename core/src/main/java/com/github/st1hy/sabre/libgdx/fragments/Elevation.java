package com.github.st1hy.sabre.libgdx.fragments;

public class Elevation {
    private static final float elevationDp = 7.5f;

    private final float elevationHigh, elevationLow;

    public Elevation(float density) {
        this.elevationLow = elevationDp * density;
        this.elevationHigh = 2f * elevationLow;
    }

    public float getElevationHigh() {
        return elevationHigh;
    }

    public float getElevationLow() {
        return elevationLow;
    }
}
