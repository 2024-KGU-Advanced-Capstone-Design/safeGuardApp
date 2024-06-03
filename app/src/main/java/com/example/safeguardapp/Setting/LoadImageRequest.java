package com.example.safeguardapp.Setting;

import com.example.safeguardapp.LogIn.LoginPageFragment;

public class LoadImageRequest {
    private String userType, userId;

    public LoadImageRequest(String userType, String userId){
        this.userId = userId;
        this.userType = userType;
    }
}
