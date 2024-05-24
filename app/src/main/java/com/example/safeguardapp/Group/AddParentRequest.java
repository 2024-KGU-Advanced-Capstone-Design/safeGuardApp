package com.example.safeguardapp.Group;

public class AddParentRequest {
    private String parentId;
    private String childName;

    public AddParentRequest(String parentId, String childName){
        this.parentId = parentId;
        this.childName = childName;
    }
}
