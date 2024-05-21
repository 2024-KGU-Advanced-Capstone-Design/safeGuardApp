package com.example.safeguardapp.LogIn;

import com.google.gson.annotations.SerializedName;

public class LoginRequest{

    @SerializedName("editTextID")
    public String editTextID;

    @SerializedName("editTextPW")
    public String editTextPW;

    @SerializedName("loginType")
    public String loginType;

    @SerializedName("fcmToken")
    public String fcmToken;

    public LoginRequest(String editTextID, String editTextPW, String loginType, String fcmToken){
        this.editTextID = editTextID;
        this.editTextPW = editTextPW;
        this.loginType = loginType;
        this.fcmToken = fcmToken;
    }
}

