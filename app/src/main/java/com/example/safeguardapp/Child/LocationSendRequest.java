package com.example.safeguardapp.Child;

public class LocationSendRequest {
    private String type, id;
    private double latitude, longitude, battery;
    public LocationSendRequest(String type, String id, double latitude, double longitude, double battery){
        this.type = type;
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.battery = battery;
    }
}
