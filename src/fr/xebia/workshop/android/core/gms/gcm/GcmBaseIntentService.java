package fr.xebia.workshop.android.core.gms.gcm;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import static com.google.android.gms.gcm.GoogleCloudMessaging.MESSAGE_TYPE_DELETED;
import static com.google.android.gms.gcm.GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE;

public abstract class GcmBaseIntentService extends IntentService {

    public static final String TAG = "GCMBaseIntentService";

    // wakelock
    private static final String WAKELOCK_KEY = "GCM_LIB";
    private static PowerManager.WakeLock sWakeLock;

    // Java lock used to synchronize access to sWakelock
    private static final Object LOCK = GcmBaseIntentService.class;

    /**
     * Number of messages deleted by the server because the device was idle.
     * Present only on messages of special type deleted
     */
    public static final String EXTRA_TOTAL_DELETED = "total_deleted";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GcmBaseIntentService(String name) {
        super(name);
    }

    /**
     * Called when a cloud message has been received.
     *
     * @param context application's context.
     * @param intent  intent containing the message payload as extras.
     */
    protected abstract void onMessage(Context context, Intent intent);

    /**
     * Called when the GCM server tells pending messages have been deleted
     * because the device was idle.
     *
     * @param context application's context.
     * @param total   total number of collapsed messages
     */
    protected void onDeletedMessages(Context context, int total) {
    }

    @Override
    public final void onHandleIntent(Intent intent) {
        try {
            Context context = getApplicationContext();
            String messageType = GoogleCloudMessaging.getInstance(context).getMessageType(intent);

            if (MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                onMessage(context, intent);
            } else if (MESSAGE_TYPE_DELETED.equals(messageType)) {
                onDeletedMessages(context, intent.getIntExtra(EXTRA_TOTAL_DELETED, 0));
            } else {
                Log.w(TAG, "unhandled message : " + intent);
            }

        } finally {
            // Release the power lock
        }
    }

    /**
     * Called from the broadcast receiver.
     * <p/>
     * Will process the received intent, call handleMessage()
     * etc. in background threads, with a wake lock, while keeping the service
     * alive.
     */
    public static void runIntentInService(Context context, Intent intent, Class clazz) {
        synchronized (LOCK) {
            if (sWakeLock == null) {
                // This is called from BroadcastReceiver, there is no init.
                PowerManager pm = (PowerManager)
                        context.getSystemService(Context.POWER_SERVICE);
                sWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        WAKELOCK_KEY);
            }
        }
        Log.v(TAG, "Acquiring wakelock");
        sWakeLock.acquire();
        intent.setClassName(context, clazz.getName());
        context.startService(intent);
    }

}
