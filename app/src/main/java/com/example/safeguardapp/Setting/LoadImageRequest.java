package com.example.safeguardapp.Setting;


public class LoadImageRequest {
    private String userType, userId;

    public LoadImageRequest(String userType, String userId){
        this.userId = userId;
        this.userType = userType;
    }
}
