package com.example.safeguardapp;

import com.google.gson.annotations.SerializedName;

public class SectorMapResponse {
    @SerializedName("status")
    private String resultCode;

    public String getResultCode() {
        return resultCode;
    }
    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }
}
