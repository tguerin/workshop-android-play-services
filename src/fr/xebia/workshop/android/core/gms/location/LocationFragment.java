package fr.xebia.workshop.android.core.gms.location;

import android.app.PendingIntent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import fr.xebia.workshop.android.core.utils.Time;

import java.util.Arrays;

public class LocationFragment extends Fragment implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationListener, LocationClient.OnAddGeofencesResultListener {

    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 20;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            Time.MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 10;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL = Time.MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;


    private static final int LOCATION_CONNECTION_FAILURE_REQUEST = 9000;
    private static final String TAG_LOCATION = "LocationFragment";

    private LocationClient locationClient;
    private LocationRequest currentlocationRequest;
    private PendingIntent pendingIntentForGeofence;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        locationClient = new LocationClient(getActivity().getApplicationContext(), this, this);

        // Update request
        currentlocationRequest = LocationRequest.create();
        // Use high accuracy
        currentlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        currentlocationRequest.setInterval(UPDATE_INTERVAL);
        currentlocationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationClient.connect();
    }


    @Override
    public void onDestroy() {
        locationClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {
        locationClient.requestLocationUpdates(currentlocationRequest, this);
        if(pendingIntentForGeofence != null){
            registerGeofence(pendingIntentForGeofence);
            pendingIntentForGeofence = null;
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
                Toast.makeText(getActivity(), "Error code received : " +e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getActivity(), "Error code received : " + connectionResult.getErrorCode(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(getActivity() instanceof LocationListener) {
            ((LocationListener) getActivity()).onLocationChanged(location);
        }
    }


    /**
     * Attach a {@link com.google.android.gms.plus.PlusClient} managing fragment to you activity.
     *
     * @param activity          The activity to attach the fragment to.
     * @return The fragment managing a {@link com.google.android.gms.plus.PlusClient}.
     */
    public static LocationFragment getLocationFragment(FragmentActivity activity) {

        // Check if the fragment is already attached.
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(TAG_LOCATION);
        if (fragment instanceof LocationFragment) {
            // The fragment is attached.
                return (LocationFragment) fragment;
        }

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // If a fragment was already attached, remove it to clean up.
        if (fragment != null) {
            fragmentTransaction.remove(fragment);
        }

        // Create a new fragment and attach it to the fragment manager.
        LocationFragment locationFragment = new LocationFragment();
        fragmentTransaction.add(locationFragment, TAG_LOCATION);
        fragmentTransaction.commit();
        return locationFragment;
    }

    public void requestGeofence(PendingIntent pendingIntent) {
        if(locationClient == null || !locationClient.isConnected()) {
            pendingIntentForGeofence = pendingIntent;
        } else {
            registerGeofence(pendingIntent);
        }
    }

    private void registerGeofence(PendingIntent pendingIntent) {
        Geofence geofence = new Geofence.Builder()
                .setRequestId("test")
                .setCircularRegion(48.879593, 2.415551, 100)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
        locationClient.addGeofences(Arrays.asList(geofence), pendingIntent, this);
    }

    @Override
    public void onAddGeofencesResult(int i, String[] strings) {
    }
}
