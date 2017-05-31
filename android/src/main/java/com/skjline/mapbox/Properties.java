package com.skjline.mapbox;

import com.google.gson.annotations.SerializedName;

public class Properties {
    private String confidence;
    private String distance;
    private String duration;
    private int[] indices;

    @SerializedName("matchedPoints")
    private double[][] coordinates;

    public String getConfidence() {
        return confidence;
    }

    public String getDistance() {
        return distance;
    }

    public String getDuration() {
        return duration;
    }

    public double[][] getMatchedPoints() {
        return coordinates;
    }

    public int[] getIndices() {
        return indices;
    }
}
