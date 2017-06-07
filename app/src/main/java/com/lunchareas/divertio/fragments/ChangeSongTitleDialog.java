package com.lunchareas.divertio.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.activities.BaseActivity;
import com.lunchareas.divertio.activities.BaseListActivity;
import com.lunchareas.divertio.activities.MainActivity;
import com.lunchareas.divertio.models.SongData;
import com.lunchareas.divertio.utils.SongUtil;

public class ChangeSongTitleDialog extends DialogFragment {

    private static final String TAG = ChangeSongTitleDialog.class.getName();

    public static final String MUSIC_POS = "music_pos";

    private View changeTitleView;
    private EditText newTitleInput;
    private String inputText;
    private int position;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get correct position
        position = (int) getArguments().get(MUSIC_POS);
        Log.d(TAG, "Position: " + position);

        AlertDialog.Builder titleChangeDialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        changeTitleView = inflater.inflate(R.layout.change_song_title_dialog, null);
        titleChangeDialogBuilder
                .setView(changeTitleView)
                .setPositiveButton(R.string.dialog_change, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Got click on positive title change button.");

                        // Get user input
                        newTitleInput = (EditText) changeTitleView.findViewById(R.id.change_song_title_hint);
                        inputText = newTitleInput.getText().toString();

                        // Change the song name
                        SongData songData = ((BaseActivity) getActivity()).getSongInfoList().get(position);
                        SongUtil songController = new SongUtil(getActivity());
                        songController.changeSongName(songData, inputText);

                        // Re-update the view
                        ((BaseListActivity) getActivity()).setMainView();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Canceled title change.");
                    }
                });

        return titleChangeDialogBuilder.create();
    }
}
