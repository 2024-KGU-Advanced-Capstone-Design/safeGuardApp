package com.example.safeguardapp;

import com.google.gson.annotations.SerializedName;

public class CodeRequest {
    @SerializedName("inputId")
    private String userID;

    @SerializedName("inputCode")
    private String code;

    public CodeRequest(String userID, String code){
        this.userID = userID;
        this.code = code;
    }

}
