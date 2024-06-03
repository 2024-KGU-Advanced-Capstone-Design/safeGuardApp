package com.example.safeguardapp.Setting;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Multipart;

public class SendImageRequest {
    private String uploaderType;
    private String uploaderId;
    public SendImageRequest(String uploaderType, String uploaderId){
        this.uploaderType = uploaderType;
        this.uploaderId = uploaderId;
    }
}
