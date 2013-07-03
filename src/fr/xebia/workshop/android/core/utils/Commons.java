package fr.xebia.workshop.android.core.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import fr.xebia.workshop.android.R;
import fr.xebia.workshop.android.ui.HomeActivity;

public class Commons {

    /**
     * Google API project id registered to use GCM.
     */
    public static final String SENDER_ID = "518611366135";

    public static final String SERVER_ROOT_URL = "http://play-services.tom404.cloudbees.net";
    //public static final String SERVER_ROOT_URL = "http://192.168.0.41:3000";

    /**
     * Intent used to display a message in the screen.
     */
    public static final String DISPLAY_MESSAGE_ACTION = "com.google.android.gcm.demo.app.DISPLAY_MESSAGE";

    /**
     * Intent's extra that contains the message to be displayed.
     */
    public static final String EXTRA_MESSAGE = "message";

    /**
     * Notifies UI to display a message.
     * <p/>
     * This method is defined in the common helper because it's used both by
     * the UI and the background service.
     *
     * @param context application's context.
     * @param message message to be displayed.
     */
    public static void displayMessage(Context context, String message) {
        context.sendBroadcast(new Intent(DISPLAY_MESSAGE_ACTION).putExtra(EXTRA_MESSAGE, message));
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    public static void notifyUser(Context context, String message) {
        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_gcm) //
                .setContentIntent(buildPendingIntent(context)) //
                .setContentTitle(context.getString(R.string.notification_title)) //
                .setContentText(message) //
                .setWhen(System.currentTimeMillis()) //
                .setAutoCancel(true) //
                .build();

        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(1, notification);
    }

    private static PendingIntent buildPendingIntent(Context context) {
        Intent notificationIntent = new Intent(context, HomeActivity.class);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context, 0, notificationIntent, 0);
    }
}
