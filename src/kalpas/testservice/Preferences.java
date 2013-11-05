package kalpas.testservice;

import android.content.Context;

public class Preferences {

    public static final String PREFS_SENDER_DEFAULT = "1234";
    public static final String        PREFS                = "kalpas.test";
    public static final String        PREFS_SENDER         = "sender";

    public static  String getSender(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(PREFS_SENDER, PREFS_SENDER_DEFAULT);
    }

}
