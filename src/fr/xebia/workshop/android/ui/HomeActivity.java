package fr.xebia.workshop.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import com.google.android.gms.plus.PlusClient;
import fr.xebia.workshop.android.core.gms.plus.PlusClientFragment;
import fr.xebia.workshop.android.R;

public class HomeActivity extends FragmentActivity implements View.OnClickListener, PlusClientFragment.OnSignedInListener {

    private static final String TAG = "HomeActivity";
    private static final int SIGNIN_REQUEST_CODE = 1;

    private PlusClientFragment plusClientFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        plusClientFragment = PlusClientFragment.getPlusClientFragment(this, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        plusClientFragment.handleOnActivityResult(requestCode, responseCode, intent);
    }

    @Override
    public void onSignedIn(PlusClient plusClient) {
        startActivity(new Intent(this, MapActivity.class));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sign_in_button:
                plusClientFragment.signIn(SIGNIN_REQUEST_CODE);
                break;
        }
    }
}
