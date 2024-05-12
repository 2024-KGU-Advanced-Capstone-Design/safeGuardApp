package com.example.safeguardapp.LogIn;

import com.google.gson.annotations.SerializedName;

public class LoginRequest{

    @SerializedName("editTextID")
    public String editTextID;

    @SerializedName("editTextPW")
    public String editTextPW;

    @SerializedName("loginType")
    public String loginType = "Member";

    public LoginRequest(String editTextID, String editTextPW) {
        this.editTextID=editTextID;
        this.editTextPW=editTextPW;
    }

    public LoginRequest(String editTextID, String editTextPW, String loginType){
        this.editTextID=editTextID;
        this.editTextPW=editTextPW;
        this.loginType=loginType;
    }

    public String getEditTextPW() {
        return editTextPW;
    }

    public String getEditTextID() {
        return editTextID;
    }

    public void setEditTextID(String editTextID) {
        this.editTextID = editTextID;
    }

    public void setEditTextPW(String editTextPW) {
        this.editTextPW = editTextPW;
    }
}

