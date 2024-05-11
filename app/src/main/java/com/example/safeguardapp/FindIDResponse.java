package com.example.safeguardapp;

import com.google.gson.annotations.SerializedName;

public class FindIDResponse {

    @SerializedName("status")
    private String resultCode;

    @SerializedName("memberId")
    private String memberId;

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

}
