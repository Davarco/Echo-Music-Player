package com.lunchareas.divertio.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.activities.BaseActivity;
import com.lunchareas.divertio.activities.BaseListActivity;
import com.lunchareas.divertio.activities.MainActivity;
import com.lunchareas.divertio.models.SongDBHandler;
import com.lunchareas.divertio.models.SongData;
import com.lunchareas.divertio.utils.SongUtil;

public class ChangeSongArtistDialog extends DialogFragment {

    private static final String TAG = ChangeSongTitleDialog.class.getName();

    public static final String MUSIC_POS = "music_pos";

    private View artistChangeView;
    private EditText newArtistInput;
    private String inputText;
    private int position;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get correct position
        position = (int) getArguments().get(MUSIC_POS);
        Log.d(TAG, "Position: " + position);

        AlertDialog.Builder artistChangeDialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        artistChangeView = inflater.inflate(R.layout.change_song_artist_dialog, null);
        artistChangeDialogBuilder
                .setView(artistChangeView)
                .setPositiveButton(R.string.dialog_change, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Got click on positive change artist button.");


                        // Get user input
                        newArtistInput = (EditText) artistChangeView.findViewById(R.id.change_song_artist_hint);
                        inputText = newArtistInput.getText().toString();

                        // Change the song name
                        SongData songData = ((BaseActivity) getActivity()).getSongInfoList().get(position);
                        SongUtil songController = new SongUtil(getActivity());
                        songController.changeSongArtist(songData, inputText);

                        // Re-update the view
                        ((BaseListActivity) getActivity()).setMainView();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Canceled artist change.");
                    }
                });

        return artistChangeDialogBuilder.create();
    }
}
