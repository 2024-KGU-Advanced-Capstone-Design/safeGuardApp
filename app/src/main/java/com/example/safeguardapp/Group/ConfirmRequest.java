package com.example.safeguardapp.Group;

public class ConfirmRequest {
    private String senderId;
    private String childName;
    private String confirmType;

    public ConfirmRequest(String senderId, String childName, String confirmType){
        this.senderId = senderId;
        this.childName = childName;
        if(confirmType.equals("도착"))
            confirmType = "ARRIVED";
        else if(confirmType.equals("출발"))
            confirmType = "DEPART";
        else if(confirmType.equals("?"))
            confirmType = "UNCONFIRMED";

        this.confirmType = confirmType;
    }
}
