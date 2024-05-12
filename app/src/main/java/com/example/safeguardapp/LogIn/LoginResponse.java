package com.example.safeguardapp.LogIn;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("status")
    public String resultCode;

    @SerializedName("authorization")
    public String token;


    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}