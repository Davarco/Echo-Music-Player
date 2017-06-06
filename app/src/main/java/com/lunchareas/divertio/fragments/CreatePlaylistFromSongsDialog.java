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
import com.lunchareas.divertio.models.SongData;
import com.lunchareas.divertio.utils.PlaylistUtil;

import java.util.List;

public class CreatePlaylistFromSongsDialog extends DialogFragment {

    private static final String TAG = CreatePlaylistFromSongsDialog.class.getName();

    public static final String MUSIC_LIST = "music_list";

    private List<SongData> songList;
    private List<Integer> selectedSongIndexes;
    private View dialogView;
    private EditText playlistNameInput;
    private String playlistName;
    private PlaylistUtil playlistUtil;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get songs from activity
        selectedSongIndexes = getArguments().getIntegerArrayList(MUSIC_LIST);
        songList = ((BaseActivity) getActivity()).getSongsFromIndexes(selectedSongIndexes);

        // Get the view
        LayoutInflater inflater = getActivity().getLayoutInflater();
        dialogView = inflater.inflate(R.layout.create_playlist_dialog, null);

        // Create the util
        playlistUtil = new PlaylistUtil(getActivity());

        AlertDialog.Builder createPlaylistBuilder = new AlertDialog.Builder(getActivity());
        createPlaylistBuilder.setView(dialogView);
        createPlaylistBuilder.setPositiveButton(R.string.playlist_create_title, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Get name from input
                        playlistNameInput = (EditText) dialogView.findViewById(R.id.dialog_create_playlist_name);
                        playlistName = playlistNameInput.getText().toString().trim();
                        if (!playlistUtil.nameAlreadyExists(playlistName)) {
                            Log.d(TAG, "Playlist name: " + playlistName);
                            playlistUtil.createPlaylist(playlistName, songList);
                            ((BaseActivity) getActivity()).setMainView();
                        } else {

                            // Create dialog because name is invalid
                            Log.d(TAG, "Playlist name already exists.");
                            ((BaseActivity) getActivity()).createPlaylistNameFailureDialog();
                        }
                    }
                });
        createPlaylistBuilder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Canceled creation...");
                    }
                });
        return createPlaylistBuilder.create();
    }
}
