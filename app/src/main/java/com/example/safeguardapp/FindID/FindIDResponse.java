package com.example.safeguardapp.FindID;

import com.google.gson.annotations.SerializedName;

public class FindIDResponse {

    @SerializedName("status")
    private String resultCode;

    @SerializedName("memberId")
    private String memberId;

    public String getResultCode() {
        return resultCode;
    }

    public String getMemberId() {
        return memberId;
    }

}
