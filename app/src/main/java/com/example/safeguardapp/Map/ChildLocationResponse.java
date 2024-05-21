package com.example.safeguardapp.Map;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class ChildLocationResponse {

    private double latitude;
    private double longitude;


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
