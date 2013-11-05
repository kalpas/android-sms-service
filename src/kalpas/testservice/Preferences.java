package kalpas.testservice;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    public static final String PREFS_SENDER_DEFAULT = "1234";
    public static final String PREFS                = "kalpas.test";
    public static final String PREFS_SENDER         = "sender";

    public static String getSender(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(PREFS_SENDER, PREFS_SENDER_DEFAULT);
    }

    public static void setSender(Context context, String setting) {
        SharedPreferences.Editor preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        preferences.putString(PREFS_SENDER, setting);
        preferences.commit();
    }

    public static String getFile() {
        return "log.txt";
    }

}
