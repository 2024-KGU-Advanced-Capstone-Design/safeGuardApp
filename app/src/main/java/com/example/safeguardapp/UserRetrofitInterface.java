package com.example.safeguardapp;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserRetrofitInterface {

    //@Get ("user")
    //Call<UserDTO> getUser();

    @POST("signup")
    Call<ResponseBody> signUp(@Body SignUpRequestDTO jsonUser);
}
