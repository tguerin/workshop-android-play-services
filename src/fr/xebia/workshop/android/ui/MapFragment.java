package fr.xebia.workshop.android.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.plus.model.people.Person;

import java.io.InputStream;

public class MapFragment extends SupportMapFragment {

    private GoogleMap map;
    private Person currentPerson;
    private Marker currentPersonMarker;
    private String loadedImageUrl;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMap();
    }


    private void setUpMap() {
        map = getMap();
        if (map == null) {
            Toast.makeText(getActivity(), "Maps initialization failed", Toast.LENGTH_LONG).show();
            return;
        }
    }

    public void updatePersonInfo(Person currentPerson) {
        this.currentPerson = currentPerson;
        if (currentPersonMarker != null) {
            updateUserIconIfNecessary();
        }
    }

    private void updateUserIconIfNecessary() {
        String imageUrl = currentPerson.getImage().getUrl();
        if (!imageUrl.equals(loadedImageUrl)) {
            new DownloadImageTask().execute(imageUrl);
        }
    }

    public void updateClientLocation(Location location) {
        if (map == null) return;

        LatLng locationPosition = new LatLng(location.getLatitude(), location.getLongitude());
        if (currentPersonMarker != null) {
            currentPersonMarker.setPosition(locationPosition);
            currentPersonMarker.setDraggable(true);
        } else {
            currentPersonMarker = map.addMarker(new MarkerOptions().position(locationPosition));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(locationPosition, 15));
        }

        if (currentPerson != null) {
            updateUserIconIfNecessary();
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            currentPersonMarker.setIcon(BitmapDescriptorFactory.fromBitmap(result));
        }
    }
}
