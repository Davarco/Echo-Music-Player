package com.lunchareas.echomp.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;

import com.lunchareas.echomp.R;
import com.lunchareas.echomp.activities.MainActivity;
import com.lunchareas.echomp.utils.MediaDataUtils;


public class PlaylistCreateDialog extends DialogFragment {

    private static final String TAG = PlaylistCreateDialog.class.getName();

    private EditText input;
    private View view;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Create builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        view = View.inflate(getActivity(), R.layout.dialog_playlist_create, null);
        input = (EditText) view.findViewById(R.id.rename_playlist);
        builder.setView(view);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                long id = MediaDataUtils.createPlaylist(input.getText().toString().trim(), getContext());
                ((MainActivity) getActivity()).updateAll();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });

        return builder.create();
    }
}
