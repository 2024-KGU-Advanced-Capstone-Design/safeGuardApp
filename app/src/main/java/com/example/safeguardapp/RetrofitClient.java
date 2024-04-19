package com.example.safeguardapp;

import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient{
    private static RetrofitClient instance = null;
    private static UserRetrofitInterface userRetrofitInterface;
    private static String baseUrl = "http://192.168.219.107:8080/";

    private RetrofitClient(String state){
        if(state.equals("signup")){
            retrofit2.Retrofit retrofit = new retrofit2.Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
            userRetrofitInterface = retrofit.create(UserRetrofitInterface.class);
        }
        else if (state.equals("login")) {
            retrofit2.Retrofit retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            userRetrofitInterface = retrofit.create(UserRetrofitInterface.class);
        }
    }

    public static RetrofitClient getInstance(String state) {
        if (instance == null) {
            instance = new RetrofitClient(state);
        }
        return instance;
    }

    public UserRetrofitInterface getUserRetrofitInterface() {
        return userRetrofitInterface;
    }
}
