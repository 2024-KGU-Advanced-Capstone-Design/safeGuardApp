package com.example.safeguardapp.Emergency;

public class EmergencyRequest {
    private String senderId, childName;
    public EmergencyRequest(String senderId, String childName){
        this.senderId = senderId;
        this.childName = childName;
    }
}
