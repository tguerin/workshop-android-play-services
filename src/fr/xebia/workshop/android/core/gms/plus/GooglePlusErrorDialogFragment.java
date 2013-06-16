package fr.xebia.workshop.android.core.gms.plus;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import com.google.android.gms.plus.GooglePlusUtil;

/**
 * Wraps the {@link android.app.Dialog} returned by {@link com.google.android.gms.plus.GooglePlusUtil#getErrorDialog}
 * so that it can be properly managed by the {@link android.app.Activity}.
 */
public final class GooglePlusErrorDialogFragment extends DialogFragment {

    /**
     * The error code returned by the
     * {@link com.google.android.gms.plus.GooglePlusUtil#checkGooglePlusApp(android.content.Context)} call.
     */
    public static final String ARG_ERROR_CODE = "errorCode";

    /**
     * The request code given when calling {@link android.app.Activity#startActivityForResult}.
     */
    public static final String ARG_REQUEST_CODE = "requestCode";

    /**
     * Creates a {@link android.support.v4.app.DialogFragment}.
     */
    public GooglePlusErrorDialogFragment() {}

    /**
     * Returns a {@link android.app.Dialog} created by {@link com.google.android.gms.plus.GooglePlusUtil#getErrorDialog} with the
     * provided errorCode, activity, and request code.
     *
     * @param savedInstanceState Not used.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        return GooglePlusUtil.getErrorDialog(args.getInt(ARG_ERROR_CODE), getActivity(),
                args.getInt(ARG_REQUEST_CODE));
    }

    /**
     * Create a {@link android.support.v4.app.DialogFragment} for displaying the {@link com.google.android.gms.plus.GooglePlusUtil#getErrorDialog}.
     * @param errorCode The error code returned by
     *              {@link com.google.android.gms.plus.GooglePlusUtil#checkGooglePlusApp(android.content.Context)}
     * @param requestCode The request code for resolving the resolution activity.
     * @return The {@link android.support.v4.app.DialogFragment}.
     */
    public static DialogFragment create(int errorCode, int requestCode) {
        DialogFragment fragment = new GooglePlusErrorDialogFragment();
        Bundle args = new Bundle();
        args.putInt(GooglePlusErrorDialogFragment.ARG_ERROR_CODE, errorCode);
        args.putInt(GooglePlusErrorDialogFragment.ARG_REQUEST_CODE, requestCode);
        fragment.setArguments(args);
        return fragment;
    }
}
