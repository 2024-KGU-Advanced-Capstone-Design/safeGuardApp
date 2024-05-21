package com.example.safeguardapp.LogIn;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

public class SharedPreferenceManager {
    private static final String PREFERENCE_NAME = "appdata";

    public static SharedPreferences getPreferences(Context context){
        return context.getSharedPreferences(PREFERENCE_NAME,Context.MODE_PRIVATE);
    }

    public static void setLoginInfo(Context context, String ID, String PW){
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("ID",ID);
        editor.putString("PW",PW);

        editor.apply();
    }

    public static Map<String,String> getLoginInfo(Context context){
        SharedPreferences prefs = getPreferences(context);
        Map<String,String> LoginInfo = new HashMap<>();
        String id = prefs.getString("ID","");
        String pw = prefs.getString("PW","");

        LoginInfo.put("ID",id);
        LoginInfo.put("PW",pw);

        return LoginInfo;
    }

}
