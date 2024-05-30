package com.example.safeguardapp.Map;

public class LocationRequest {
    private String type;
    private String id;
    private double batteryLevel;

    public LocationRequest(String type, String id, double batteryLevel){
        this.type = type;
        this.id = id;
        this.batteryLevel = batteryLevel;
    }
}
