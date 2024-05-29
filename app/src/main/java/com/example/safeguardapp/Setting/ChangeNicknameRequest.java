package com.example.safeguardapp.Setting;

public class ChangeNicknameRequest {
    private String userID;
    private String nickname;

    public ChangeNicknameRequest(String userID, String nickname){
        this.userID = userID;
        this.nickname = nickname;
    }
}
