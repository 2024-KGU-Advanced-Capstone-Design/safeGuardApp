package com.example.safeguardapp;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    public static final String preferenceName = "appdata";
    public static SharedPreferences prefs;
    public static SharedPreferences.Editor editor;

    private static SharedPreferences getPreferences(Context context){
        return context.getSharedPreferences(preferenceName,Context.MODE_PRIVATE);
    }

    public static void setPreference(Context context, String ID, String PW, String loginType){
        prefs = getPreferences(context);
        editor = prefs.edit();

        editor.putString("inputID",ID);
        editor.putString("inputPW",PW);
        editor.putString("loginType",loginType);

        editor.commit();
    }

    public static void clear(Context context){
        prefs = getPreferences(context);
        editor = prefs.edit();
        editor.clear();
        editor.commit();
    }

}
