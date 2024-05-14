package com.example.safeguardapp.Group;

public class ResetChildPWRequest {
    private String id, newPassword;

    public ResetChildPWRequest(String id, String newPassword){
        this.id = id;
        this.newPassword = newPassword;
    }
}
