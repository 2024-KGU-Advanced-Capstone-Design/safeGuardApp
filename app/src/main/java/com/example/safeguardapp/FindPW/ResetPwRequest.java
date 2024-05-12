package com.example.safeguardapp.FindPW;

public class ResetPwRequest {
    private String id;
    private String newPassword;

    public ResetPwRequest(String id){
        this.id = id;
    }

    public ResetPwRequest(String id, String newPassword){
        this.id = id;
        this.newPassword = newPassword;
    }

}
