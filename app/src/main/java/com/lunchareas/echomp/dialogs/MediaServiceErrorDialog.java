package com.lunchareas.echomp.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.app.DialogFragment;
import android.view.View;

import com.lunchareas.echomp.R;

public class MediaServiceErrorDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Build the error dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(View.inflate(getActivity(), R.layout.dialog_media_service_error, null));
        return builder.create();
    }
}
