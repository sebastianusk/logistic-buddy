package com.batp.logisticbuddy.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.batp.logisticbuddy.LoginActivity;

/**
 * Created by nisie on 9/10/16.
 */
public class SessionHandler {

    public static final String DRIVER = "driver1@batp.com";
    public static final String SERVER = "server@batp.com";
    public static final String CLIENT = "client1@batp.com";
    public static final String MASTER = "master@batp.com";
    private static final String SESSION = "SESSION";
    private static final String ROLE = "ROLE";
    private static final String USER_ID = "USER_ID";

    public static void setSession(Context context, String role, String userId) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(SESSION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putString(ROLE, role);
        editor.putString(USER_ID,userId);
        editor.apply();
    }


    public static String getSession(Context context) {
        return context.getSharedPreferences(SESSION, Context.MODE_PRIVATE).getString(ROLE,"");
    }

    public static String getCurrentDriver(Context context) {
        //TODO GET DRIVER ID
        return context.getSharedPreferences(SESSION, Context.MODE_PRIVATE).getString(USER_ID,"");
    }

    public static String getUserId(Context context) {
        return context.getSharedPreferences(SESSION, Context.MODE_PRIVATE).getString(USER_ID,"");
    }

    public static void setSession(Context context, String role) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(SESSION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putString(ROLE, role);
        editor.apply();
    }


}
