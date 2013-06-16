package fr.xebia.workshop.android.ui;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.model.people.Person;
import fr.xebia.workshop.android.R;
import fr.xebia.workshop.android.core.gms.location.ActivityRecognitionFragment;
import fr.xebia.workshop.android.core.gms.location.LocationFragment;
import fr.xebia.workshop.android.core.gms.plus.PlusClientFragment;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class MapActivity extends FragmentActivity implements PlusClientFragment.OnSignedInListener, LocationListener {

    private MapFragment mapFragment;
    private LocationFragment locationFragment;
    private PlusClientFragment plusClientFragment;
    private ActivityRecognitionFragment activityRecognitionFragment;

    private ActivityRecognitionReceiver activityRecognitionReceiver;


    private static final int SIGN_IN = 1;
    private Person currentPerson;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        plusClientFragment = PlusClientFragment.getPlusClientFragment(this, null);
        locationFragment = LocationFragment.getLocationFragment(this);
        activityRecognitionFragment = ActivityRecognitionFragment.getActivityRecognitionFragment(this);
        activityRecognitionReceiver = new ActivityRecognitionReceiver();


        if (savedInstanceState == null) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ActivityRecognitionReceiver.ACTION),
                    FLAG_UPDATE_CURRENT);
            activityRecognitionFragment.requestActivityUpdates(pendingIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(activityRecognitionReceiver, new IntentFilter(ActivityRecognitionReceiver.ACTION));
        if (currentPerson == null) {
            plusClientFragment.signIn(SIGN_IN);
        }
    }

    @Override
    protected void onStop() {
        unregisterReceiver(activityRecognitionReceiver);
        super.onStop();
    }

    @Override
    public void onSignedIn(PlusClient plusClient) {
        currentPerson = plusClient.getCurrentPerson();
        mapFragment.updatePersonInfo(currentPerson);
    }

    @Override
    public void onLocationChanged(Location location) {
        mapFragment.updateClientLocation(location);
    }

    public static final class ActivityRecognitionReceiver extends BroadcastReceiver {

        public static final String ACTION = "fr.xebia.workshop.map.activity.recognition";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ActivityRecognitionResult.hasResult(intent)) {
                // Get the update
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                // Get the most probable activity
                DetectedActivity mostProbableActivity = result.getMostProbableActivity();
                /*
                 * Get the probability that this activity is the
                 * the user's actual activity
                 */
                int confidence = mostProbableActivity.getConfidence();

                /*
                 * Get an integer describing the type of activity
                 */
                int activityType = mostProbableActivity.getType();
                String activityName = getNameFromType(activityType);

                Toast.makeText(context, "Activity : " + activityName + " confidence : " + confidence, Toast.LENGTH_LONG).show();
            } else if (LocationClient.hasError(intent)){
                Log.e("Location service error ", Integer.toString(LocationClient.getErrorCode(intent)));
            }
        }

        private String getNameFromType(int activityType) {
            switch (activityType) {
                case DetectedActivity.IN_VEHICLE:
                    return "in_vehicle";
                case DetectedActivity.ON_BICYCLE:
                    return "on_bicycle";
                case DetectedActivity.ON_FOOT:
                    return "on_foot";
                case DetectedActivity.STILL:
                    return "still";
                case DetectedActivity.UNKNOWN:
                    return "unknown";
                case DetectedActivity.TILTING:
                    return "tilting";
            }
            return "unknown";
        }
    }
}
