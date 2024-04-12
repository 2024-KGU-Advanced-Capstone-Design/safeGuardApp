package com.example.safeguardapp;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient{
    private static RetrofitClient instance = null;
    private static UserRetrofitInterface userRetrofitInterface;
    private static String baseUrl = "http://10.0.2.2:8080/signup";

    private RetrofitClient(){
        retrofit2.Retrofit retrofit = new retrofit2.Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        userRetrofitInterface = retrofit.create(UserRetrofitInterface.class);
    }

}
