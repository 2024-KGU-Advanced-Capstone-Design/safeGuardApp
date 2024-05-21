package com.example.safeguardapp.Map;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class ChildLocationResponse {

    @SerializedName("status")
    private String resultCode;

    @SerializedName("return-coordinate")
    private HashMap<String, Double> childCoordinate;

    public String getResultCode() { return resultCode; }

    public HashMap<String, Double> getChildCoordinate() { return childCoordinate; }
}
