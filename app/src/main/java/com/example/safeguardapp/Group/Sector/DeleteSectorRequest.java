package com.example.safeguardapp.Group.Sector;

public class DeleteSectorRequest {
    private String areaID;
    private String childName;
    private String memberID;
    public DeleteSectorRequest(String areaID, String childName, String memberID){
        this.areaID = areaID;
        this.childName = childName;
        this.memberID = memberID;
    }
}
