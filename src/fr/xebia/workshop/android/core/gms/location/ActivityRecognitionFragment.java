package fr.xebia.workshop.android.core.gms.location;

import android.app.PendingIntent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.List;

public class ActivityRecognitionFragment extends Fragment implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private static final int LOCATION_CONNECTION_FAILURE_REQUEST = 9000;
    private static final String TAG_ACTIVITY = "ActivityRecognitionFragment";
    private static final long DEFAULT_UPDATE_INTERVAL = 30 * 1000;

    private ActivityRecognitionClient activityRecognitionClient;

    private List<ActivvityRecognitionRequest> requestsToRegister = new ArrayList<ActivvityRecognitionRequest>();;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        activityRecognitionClient = new ActivityRecognitionClient(getActivity().getApplicationContext(), this, this);
        activityRecognitionClient.connect();
    }


    @Override
    public void onDestroy() {
        activityRecognitionClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {
        for(ActivvityRecognitionRequest activvityRecognitionRequest : requestsToRegister) {
            activityRecognitionClient.requestActivityUpdates(activvityRecognitionRequest.updateInterval, activvityRecognitionRequest.pendingIntent);
        }
    }

    public void requestActivityUpdates(PendingIntent pendingIntent){
        requestActivityUpdates(DEFAULT_UPDATE_INTERVAL, pendingIntent);
    }

    public void requestActivityUpdates(long updateInterval, PendingIntent pendingIntent){
        if(activityRecognitionClient== null || !activityRecognitionClient.isConnected()){
            requestsToRegister.add(new ActivvityRecognitionRequest(updateInterval, pendingIntent));
        }
    }

    @Override
    public void onDisconnected() {
        // nothing to do
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(), LOCATION_CONNECTION_FAILURE_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                Toast.makeText(getActivity(), "Error code received : " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getActivity(), "Error code received : " + connectionResult.getErrorCode(), Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Attach a {@link com.google.android.gms.plus.PlusClient} managing fragment to you activity.
     *
     * @param activity The activity to attach the fragment to.
     * @return The fragment managing a {@link com.google.android.gms.plus.PlusClient}.
     */
    public static ActivityRecognitionFragment getActivityRecognitionFragment(FragmentActivity activity) {

        // Check if the fragment is already attached.
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(TAG_ACTIVITY);
        if (fragment instanceof ActivityRecognitionFragment) {
            // The fragment is attached.
            return (ActivityRecognitionFragment) fragment;
        }

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // If a fragment was already attached, remove it to clean up.
        if (fragment != null) {
            fragmentTransaction.remove(fragment);
        }

        // Create a new fragment and attach it to the fragment manager.
        ActivityRecognitionFragment activityRecognitionFragment = new ActivityRecognitionFragment();
        fragmentTransaction.add(activityRecognitionFragment, TAG_ACTIVITY);
        fragmentTransaction.commit();
        return activityRecognitionFragment;
    }

    private static final class ActivvityRecognitionRequest{
        public final PendingIntent pendingIntent;
        public final long updateInterval;

        private ActivvityRecognitionRequest(long updateInterval, PendingIntent pendingIntent) {
            this.pendingIntent = pendingIntent;
            this.updateInterval = updateInterval;
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