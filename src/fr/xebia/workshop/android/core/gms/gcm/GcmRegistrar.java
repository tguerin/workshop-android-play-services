package fr.xebia.workshop.android.core.gms.gcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import fr.xebia.workshop.android.core.utils.Commons;
import fr.xebia.workshop.android.core.utils.Preferences;
import fr.xebia.workshop.android.core.utils.ServerUtils;

import java.io.IOException;

public class GcmRegistrar {

    private static final String PREFERENCES = "com.google.android.gcm";
    private static final String TAG = GcmRegistrar.class.getSimpleName();
    /** Default lifespan (7 days) */
    // NOTE: cannot use TimeUnit.DAYS because it's not available on API Level 8
    public static final long DEFAULT_ON_SERVER_LIFESPAN_MS = 1000 * 3600 * 24 * 7;
    private static final String PROPERTY_REG_ID = "regId";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String PROPERTY_ON_SERVER = "onServer";
    private static final String PROPERTY_ON_SERVER_EXPIRATION_TIME = "onServerExpirationTime";
    private static final String PROPERTY_ON_SERVER_LIFESPAN = "onServerLifeSpan";


    public static void register(Context context) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String registrationId = GcmRegistrar.getRegistrationId(context);
            Long userId = Preferences.getUserId(context);
            if (registrationId == null || GcmRegistrar.isRegistrationExpired(context)) {
                registrationId = gcm.register(Commons.SENDER_ID);
                if (ServerUtils.registerForGcm(registrationId, telephonyManager.getDeviceId(), userId)) {
                    setRegisteredOnServer(context, true);
                    // Save the registrationId - no need to register again.
                    setRegistrationId(context, registrationId);
                }
            }

        } catch (IOException ex) {
            Log.w(TAG, ex);
            Commons.displayMessage(context, "Problem while communicating with server");
        } finally {
            gcm.close();
        }
    }

    public static String setRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String oldRegistrationId = prefs.getString(PROPERTY_REG_ID, "");
        int appVersion = getAppVersion(context);
        Log.v(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
        return oldRegistrationId;
    }

    /** Sets whether the device was successfully registered in the server side. */
    public static void setRegisteredOnServer(Context context, boolean flag) {
        final SharedPreferences prefs = getGcmPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PROPERTY_ON_SERVER, flag);
        // set the flag's expiration date
        long lifespan = getRegisterOnServerLifespan(context);
        long expirationTime = System.currentTimeMillis() + lifespan;
        editor.putLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, expirationTime);
        editor.commit();
    }

    /**
     * Gets how long (in milliseconds) the server registration
     * property is valid.
     *
     * @return value set by {@link #setRegisteredOnServer(android.content.Context, boolean)} or
     *         {@link #DEFAULT_ON_SERVER_LIFESPAN_MS} if not set.
     */
    public static long getRegisterOnServerLifespan(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        long lifespan = prefs.getLong(PROPERTY_ON_SERVER_LIFESPAN,
                DEFAULT_ON_SERVER_LIFESPAN_MS);
        return lifespan;
    }

    /**
     * Gets the current registration id for application on GCM service.
     * <p/>
     * If result is empty, the registration has failed.
     *
     * @return registration id, or empty string if the registration is not
     *         complete.
     */
    public static String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, null);
        if (registrationId.length() == 0) {
            Log.v(TAG, "Registration not found.");
            return "";
        }
        // check if app was updated; if so, it must clear registration id to
        // avoid a race condition if GCM sends a message
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion || isRegistrationExpired(context)) {
            Log.v(TAG, "App version changed or registration expired.");
            return "";
        }
        return registrationId;
    }

    /** @return Application's version code from the {@code PackageManager}. */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Checks if the registration has expired.
     * <p/>
     * <p>To avoid the scenario where the device sends the registration to the
     * server but the server loses it, the app developer may choose to re-register
     * after REGISTRATION_EXPIRY_TIME_MS.
     *
     * @return true if the registration has expired.
     */
    private static boolean isRegistrationExpired(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        // checks if the information is not stale
        long expirationTime =
                prefs.getLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, -1);
        return System.currentTimeMillis() > expirationTime;
    }

    /** @return Application's {@code SharedPreferences}. */
    private static SharedPreferences getGcmPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

}
