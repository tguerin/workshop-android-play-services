package fr.xebia.workshop.android.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.plus.model.people.Person;
import fr.xebia.workshop.android.R;

import java.io.InputStream;

public class MapFragment extends SupportMapFragment implements GoogleMap.InfoWindowAdapter {

    private GoogleMap map;
    private Person currentPerson;
    private String currentPersonActivity = "Searching activity...";
    private Marker currentPersonMarker;
    private String loadedImageUrl;
    private ActivityRecognitionReceiver activityRecognitionReceiver;
    private Bitmap mIcon11;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        activityRecognitionReceiver = new ActivityRecognitionReceiver();
        getActivity().registerReceiver(activityRecognitionReceiver, new IntentFilter(ActivityRecognitionReceiver.ACTION));
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMap();
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(activityRecognitionReceiver);
        super.onDestroy();
    }

    private void setUpMap() {
        map = getMap();
        if (map == null) {
            Toast.makeText(getActivity(), "Maps initialization failed", Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
        map.setInfoWindowAdapter(this);
        map.addCircle(new CircleOptions().center(new LatLng(48.879593, 2.415551)).radius(100));
    }

    public void updatePersonInfo(Person currentPerson) {
        this.currentPerson = currentPerson;
        if (currentPersonMarker != null) {
            updateUserIconIfNecessary();
        }
    }

    private void updateUserIconIfNecessary() {
        String imageUrl = currentPerson.getImage().getUrl().replaceAll("sz=50", "sz=200");
        if (!imageUrl.equals(loadedImageUrl)) {
            new DownloadImageTask().execute(imageUrl);
        }
    }

    public void updateClientLocation(Location location) {
        if (map == null) return;

        LatLng locationPosition = new LatLng(location.getLatitude(), location.getLongitude());
        if (currentPersonMarker != null) {
                currentPersonMarker.setPosition(locationPosition);
        } else {
            currentPersonMarker = map.addMarker(new MarkerOptions().position(locationPosition));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(locationPosition, 15));
        }

        if (currentPerson != null) {
            updateUserIconIfNecessary();
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.map_window, null);
        ImageView imageView = (ImageView) v.findViewById(R.id.avatar);

        TextView userNameTxtView = (TextView) v.findViewById(R.id.user_name);

        TextView userActivityTxtView = (TextView) v.findViewById(R.id.user_activity);

        userNameTxtView.setText(currentPerson.getDisplayName());

        userActivityTxtView.setText(currentPersonActivity == null ? "" : currentPersonActivity);

        imageView.setBackgroundDrawable(new BitmapDrawable(mIcon11));
        // Returning the view containing InfoWindow contents
        return v;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        protected Bitmap doInBackground(String... urls) {
            loadedImageUrl = urls[0];
            mIcon11 = null;
            try {
                InputStream in = new java.net.URL(loadedImageUrl).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            currentPersonMarker.setIcon(BitmapDescriptorFactory.fromBitmap(ThumbnailUtils.extractThumbnail(mIcon11, 50, 50)));
        }
    }

    public final class ActivityRecognitionReceiver extends BroadcastReceiver {

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
                currentPersonActivity = getNameFromType(activityType) + " with confidence " + confidence;
                if(currentPersonMarker.isInfoWindowShown()){
                    currentPersonMarker.showInfoWindow();
                }
            } else if (LocationClient.hasError(intent)) {
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

    public final class GeofenceReceiver extends BroadcastReceiver {

        public static final String ACTION = "fr.xebia.workshop.map.activity.recognition";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (LocationClient.hasError(intent)) {
                Log.e("Location service error ", Integer.toString(LocationClient.getErrorCode(intent)));
            } else {
                int transitionType = LocationClient.getGeofenceTransition(intent);
                if(transitionType == Geofence.GEOFENCE_TRANSITION_ENTER){
                    // TODO notify enter
                } else {
                    // TODO notify exit
                }
            }
        }
    }
}
