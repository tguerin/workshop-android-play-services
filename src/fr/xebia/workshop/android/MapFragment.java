package fr.xebia.workshop.android;

import android.os.Bundle;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

public class MapFragment extends SupportMapFragment {

    private GoogleMap mMap;

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
        mMap = getMap();
        if (mMap == null) {
            Toast.makeText(getActivity(), "Maps initialization failed", Toast.LENGTH_LONG).show();
            return;
        }

        mMap.setMyLocationEnabled(true);
    }
}
