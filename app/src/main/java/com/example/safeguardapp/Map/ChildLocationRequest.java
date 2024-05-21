package com.example.safeguardapp.Map;

import com.google.gson.annotations.SerializedName;

public class ChildLocationRequest {
    @SerializedName("type")
    private String type;

    @SerializedName("childId")
    private String childId;

    public ChildLocationRequest(String type, String childId){
        this.type = type;
        this.childId = childId;
    }
}
