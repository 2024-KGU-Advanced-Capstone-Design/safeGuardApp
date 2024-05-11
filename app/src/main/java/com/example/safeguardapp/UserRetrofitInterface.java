package com.example.safeguardapp;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserRetrofitInterface {
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("signup")
    Call<ResponseBody> signUp(@Body SignUpRequestDTO jsonUser);

    @POST("find-member-id")
    Call<FindIDResponse> findID(@Body FindIDRequest request);

    @POST("verification-email-request")
    Call<ResponseBody> sendCode(@Body EmailRequest jsonUser);

    @POST("verification-email")
    Call<ResponseBody> codeCheck(@Body CodeRequest jsonUser);

    @POST("reset-member-password")
    Call<ResponseBody> resetPw(@Body ResetPwRequest jsonUser);

    @POST("childsignup")
    Call<ResponseBody> child(@Body ChildDTO jsonUser);


}
