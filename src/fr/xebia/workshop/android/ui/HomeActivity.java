package fr.xebia.workshop.android.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.plus.PlusClient;
import fr.xebia.workshop.android.R;
import fr.xebia.workshop.android.core.gms.gcm.GcmRegistrationIntentService;
import fr.xebia.workshop.android.core.gms.plus.PlusClientFragment;
import fr.xebia.workshop.android.core.utils.Preferences;
import fr.xebia.workshop.android.core.utils.ProgressDialogFragment;
import fr.xebia.workshop.android.core.utils.ServerUtils;

public class HomeActivity extends FragmentActivity implements View.OnClickListener, PlusClientFragment.OnSignedInListener {

    private static final int SIGNIN_REQUEST_CODE = 1;

    private PlusClientFragment plusClientFragment;
    private HomeActivity.RegistrationAsyncTask registrationAsyncTask;

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
        if (Preferences.isRegistered(this)) {
            startService(new Intent(HomeActivity.this, GcmRegistrationIntentService.class));
            startActivity(new Intent(this, MapActivity.class));
        } else {
            registrationAsyncTask = new RegistrationAsyncTask();
            registrationAsyncTask.execute(plusClient);
        }
    }

    @Override
    protected void onStop() {
        if (registrationAsyncTask != null) {
            registrationAsyncTask.cancel(true);
        }
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                plusClientFragment.signIn(SIGNIN_REQUEST_CODE);
                break;
        }
    }

    private final class RegistrationAsyncTask extends AsyncTask<PlusClient, Void, Void> {
        private static final String TAG_PROGRESS_DIALOG = "userRegistrationProgressDialog";
        private Long userId;
        private ProgressDialogFragment progressDialogFragment;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialogFragment = ProgressDialogFragment.create();
            progressDialogFragment.show(getSupportFragmentManager(), TAG_PROGRESS_DIALOG);
            progressDialogFragment.setCancelable(true);
        }

        @Override
        protected Void doInBackground(PlusClient... plusClients) {
            userId = ServerUtils.registerUser(plusClients[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialogFragment.dismiss();
            if (userId != null) {
                Preferences.setUserId(HomeActivity.this, userId);
                startService(new Intent(HomeActivity.this, GcmRegistrationIntentService.class));
                startActivity(new Intent(HomeActivity.this, MapActivity.class));
            } else {
                Toast.makeText(HomeActivity.this, "Couldn't register user", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            progressDialogFragment.dismiss();
            super.onCancelled();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (registrationAsyncTask != null) {
            registrationAsyncTask.cancel(true);
        }
    }
}
