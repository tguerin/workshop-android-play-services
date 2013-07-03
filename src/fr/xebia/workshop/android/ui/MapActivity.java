package fr.xebia.workshop.android.ui;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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


        if (savedInstanceState == null) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(MapFragment.ActivityRecognitionReceiver.ACTION),
                    FLAG_UPDATE_CURRENT);
            activityRecognitionFragment.requestActivityUpdates(pendingIntent);

            pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(MapFragment.ActivityRecognitionReceiver.ACTION),
                    FLAG_UPDATE_CURRENT);
            locationFragment.requestGeofence(pendingIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentPerson == null) {
            plusClientFragment.signIn(SIGN_IN);
        }
    }

    @Override
    protected void onStop() {
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


}
