package com.example.safeguardapp.Map;

import com.google.gson.annotations.SerializedName;

public class ChildLocationRequest {
    private String type;

    private String id;

    public ChildLocationRequest(String type, String id){
        this.type = type;
        this.id = id;
    }
}
