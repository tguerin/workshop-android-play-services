package fr.xebia.workshop.android.core.gms.gcm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GcmBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        GcmBaseIntentService.runIntentInService(context, intent, GcmIntentService.class);
        setResult(Activity.RESULT_OK, null, null);
    }
}




