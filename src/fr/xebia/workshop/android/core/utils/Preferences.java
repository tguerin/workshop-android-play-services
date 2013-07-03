package fr.xebia.workshop.android.core.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {


    private static final String PREFERENCES = "fr.xebia.workshop.android";
    private static final String PROPERTY_USER_ID = "userId";

    public static void setUserId(Context context, Long userId) {
        final SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(PROPERTY_USER_ID, userId);
        editor.commit();
    }

    public static Long getUserId(Context context) {
        return getPreferences(context).getLong(PROPERTY_USER_ID, -1);
    }

    public static boolean isRegistered(Context context) {
        return getPreferences(context).getLong(PROPERTY_USER_ID, 0l) > 0;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }
}
