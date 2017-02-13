package com.lunchareas.divertio;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class UploadSongFailureDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder uploadFailureBuilder = new AlertDialog.Builder(getActivity());
        uploadFailureBuilder
            .setMessage(R.string.upload_failure_message)
            .setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
        return uploadFailureBuilder.create();
    }
}
