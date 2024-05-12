package com.example.safeguardapp.FindPW;

import com.google.gson.annotations.SerializedName;

public class EmailRequest {
    @SerializedName("inputId")
    private String userID;

    public EmailRequest(String userID) {
        this.userID = userID;
    }
}
