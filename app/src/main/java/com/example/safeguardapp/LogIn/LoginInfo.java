package com.example.safeguardapp.LogIn;

public class LoginInfo {
    public static String getLoginID() {
        return loginID;
    }

    public static void setLoginID(String loginID) {
        LoginInfo.loginID = loginID;
    }

    private static String loginID;

}
