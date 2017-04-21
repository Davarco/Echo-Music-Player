package com.lunchareas.divertio.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.activities.BaseActivity;
import com.lunchareas.divertio.activities.MainActivity;
import com.lunchareas.divertio.models.PlaylistData;
import com.lunchareas.divertio.models.SongData;
import com.lunchareas.divertio.utils.PlaylistUtil;

import java.util.ArrayList;
import java.util.List;

public class AddToPlaylistDialog extends DialogFragment {

    private static final String TAG = AddToPlaylistDialog.class.getName();

    public static final String MUSIC_POS = "music_pos";

    private int position;
    private List<PlaylistData> playlistInfoList;
    private List<String> playlistInfoTemp;
    private List<Integer> selectedPlaylists;
    private SongData songData;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get the song data
        songData = ((BaseActivity) getActivity()).getSongInfoList().get(position);

        // Get the list of playlists to pick from
        playlistInfoList = ((BaseActivity)getActivity()).getPlaylistInfoList();
        playlistInfoTemp = new ArrayList<>();
        for (int i = 0; i < playlistInfoList.size(); i++) {
            if (!playlistInfoList.get(i).getSongList().contains(songData)) {
                playlistInfoTemp.add(playlistInfoList.get(i).getPlaylistName());
                Log.d(TAG, "Adding playlist to pick from because it does not already contain the song.");
            } else {
                Log.d(TAG, "Song already is in the playlist.");
            }
        }

        String[] playlistList = new String[playlistInfoTemp.size()];
        playlistList = playlistInfoTemp.toArray(playlistList);
        selectedPlaylists = new ArrayList<>();

        // Get correct position
        position = (int) getArguments().get(MUSIC_POS);
        Log.d(TAG, "Position: " + position);

        AlertDialog.Builder addSongDialogBuilder = new AlertDialog.Builder(getActivity());
        addSongDialogBuilder
                .setTitle(R.string.song_to_playlist_title)
                .setMultiChoiceItems(playlistList, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            selectedPlaylists.add(which);
                            Log.d(TAG, "Adding position " + which);
                        } else {
                            selectedPlaylists.remove(Integer.valueOf(which));
                            Log.d(TAG, "Removing position " + which);
                        }
                    }
                })
                .setPositiveButton(R.string.song_to_playlist_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addToPlaylists();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Canceled adding to playlist...");
                    }
                });

        return addSongDialogBuilder.create();
    }

    private void addToPlaylists() {

        // Get the song
        songData = ((BaseActivity) getActivity()).getSongInfoList().get(position);

        // Add song to playlists
        PlaylistUtil playlistUtil = new PlaylistUtil(getActivity());
        for (Integer idx: selectedPlaylists) {
            PlaylistData playlistData = playlistInfoList.get(idx);
            playlistUtil.addSongToPlaylist(songData, playlistData);
        }
    }
}
