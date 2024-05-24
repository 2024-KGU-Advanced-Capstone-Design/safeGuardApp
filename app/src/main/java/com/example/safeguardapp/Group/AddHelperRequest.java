package com.example.safeguardapp.Group;

public class AddHelperRequest {
    private String parentId;
    private String childName;

    public AddHelperRequest(String parentId, String childName){
        this.parentId = parentId;
        this.childName = childName;
    }
}
