package com.batp.logisticbuddy;

import android.content.Context;
import android.content.SharedPreferences;

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

    public static void setSession(Context context, String role) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(SESSION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putString(ROLE, role);
        editor.apply();
    }


    public static String getSession(Context context) {
        return context.getSharedPreferences(SESSION, Context.MODE_PRIVATE).getString(ROLE,"");
    }

    public static String getCurrentDriver() {
        //TODO GET DRIVER ID
        return "driver1";
    }
}
