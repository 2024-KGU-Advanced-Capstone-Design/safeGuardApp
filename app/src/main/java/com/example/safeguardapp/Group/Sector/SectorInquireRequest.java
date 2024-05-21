package com.example.safeguardapp.Group.Sector;

public class SectorInquireRequest {
    private String childName;

    public SectorInquireRequest(String childName) {
        this.childName = childName;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }
}
