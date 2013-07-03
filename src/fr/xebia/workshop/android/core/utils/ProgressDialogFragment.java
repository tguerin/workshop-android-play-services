package fr.xebia.workshop.android.core.utils;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import fr.xebia.workshop.android.R;

public class ProgressDialogFragment extends DialogFragment {

    private static final String ARG_MESSAGE = "message";

    public static ProgressDialogFragment create(int message) {
        ProgressDialogFragment progressDialogFragment = new ProgressDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MESSAGE, message);
        progressDialogFragment.setArguments(args);
        return progressDialogFragment;
    }

    public static ProgressDialogFragment create() {
        return create(R.string.progress_message);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(getArguments().getInt(ARG_MESSAGE)));
        return progressDialog;
    }
}
