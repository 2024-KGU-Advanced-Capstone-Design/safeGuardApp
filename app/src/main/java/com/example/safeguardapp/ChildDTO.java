package com.example.safeguardapp;

import com.google.gson.annotations.SerializedName;

public class ChildDTO {
    @SerializedName("childName")
    private String childID;
    @SerializedName("childPassword")
    private String childPW;
    @SerializedName("memberId")
    private String parentID;


    public ChildDTO(String childID, String childPW, String parentID){
        this.childID = childID;
        this.childPW = childPW;
        this.parentID = parentID;
    }
}