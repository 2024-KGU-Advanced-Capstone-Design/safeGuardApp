package com.example.safeguardapp;

public class UserDTO{
    private String ID;
    private String username;
    private String email;
    private String password;

    public UserDTO(String ID, String username, String email, String password){
        this.ID = ID;
        this.username = username;
        this.email = email;
        this.password = password;
    }
}