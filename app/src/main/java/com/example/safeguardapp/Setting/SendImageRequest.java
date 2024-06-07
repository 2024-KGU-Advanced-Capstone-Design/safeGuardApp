package com.example.safeguardapp.Setting;

public class SendImageRequest {
    private String uploaderType;
    private String uploaderId;
    public SendImageRequest(String uploaderType, String uploaderId){
        this.uploaderType = uploaderType;
        this.uploaderId = uploaderId;
    }
}
