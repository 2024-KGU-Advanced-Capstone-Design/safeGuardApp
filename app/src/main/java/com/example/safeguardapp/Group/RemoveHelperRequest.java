package com.example.safeguardapp.Group;

public class RemoveHelperRequest {
    private String memberId;
    private String childName;

    public RemoveHelperRequest(String memberId, String childName){
        this.memberId = memberId;
        this.childName = childName;
    }
}
