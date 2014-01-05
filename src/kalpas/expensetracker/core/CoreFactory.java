package kalpas.expensetracker.core;

import android.content.Context;

public class CoreFactory {

    private static volatile Core instance;

    public static Core getInstance(Context context) {
        Core result = instance;
        if (result == null) {
            synchronized (CoreFactory.class) {
                if (instance == null) {
                    instance = new Core(context);
                }
            }
            return instance;
        }
        return result;
    }
}
