package com.lunchareas.divertio.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.lunchareas.divertio.R;

public class DownloadNameFailureDialog extends DialogFragment {

    private static final String TAG = DownloadNameFailureDialog.class.getName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder createNameFailureBuilder = new AlertDialog.Builder(getActivity());
        createNameFailureBuilder
                .setMessage(R.string.download_name_failure_msg)
                .setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Detected ok click on name failure.");
                    }
                });
        return createNameFailureBuilder.create();
    }
}
