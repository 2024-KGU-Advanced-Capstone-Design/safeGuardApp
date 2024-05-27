package com.example.safeguardapp.Emergency;

public class EmergencyRequest {
    private String senderId, childName;
    private double latitude, longitude;
    public EmergencyRequest(String senderId, String childName, double latitude, double longitude){
        this.senderId = senderId;
        this.childName = childName;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
