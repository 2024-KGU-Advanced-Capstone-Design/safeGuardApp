package com.example.safeguardapp.SignUp;

public class SignUpRequestDTO {
    private String inputID;
    private String inputName;
    private String inputEmail;
    private String inputPW;

    public SignUpRequestDTO(String inputID, String inputName, String inputEmail, String inputPW){
        this.inputID = inputID;
        this.inputName = inputName;
        this.inputEmail = inputEmail;
        this.inputPW = inputPW;
    }
}