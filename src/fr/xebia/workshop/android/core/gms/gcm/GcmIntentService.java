package fr.xebia.workshop.android.core.gms.gcm;

import android.content.Context;
import android.content.Intent;

import static fr.xebia.workshop.android.core.utils.Commons.displayMessage;
import static fr.xebia.workshop.android.core.utils.Commons.notifyUser;

public class GcmIntentService extends GcmBaseIntentService {

    public GcmIntentService() {
        super(GcmIntentService.class.getSimpleName());
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        displayMessageAndNotifyUser(context, intent.getExtras().getString("message"));
    }

    public void displayMessageAndNotifyUser(Context context, String message) {
        displayMessage(context, message);
        notifyUser(context, message);
    }
}
