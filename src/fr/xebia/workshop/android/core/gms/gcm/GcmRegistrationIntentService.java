package fr.xebia.workshop.android.core.gms.gcm;

import android.app.IntentService;
import android.content.Intent;

public class GcmRegistrationIntentService extends IntentService {

    public GcmRegistrationIntentService() {
        super(GcmRegistrationIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GcmRegistrar.register(getApplicationContext());
    }

}
