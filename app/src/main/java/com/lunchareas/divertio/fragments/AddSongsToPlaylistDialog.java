package com.lunchareas.divertio.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.activities.BaseActivity;
import com.lunchareas.divertio.models.PlaylistData;
import com.lunchareas.divertio.models.SongData;
import com.lunchareas.divertio.utils.PlaylistUtil;

import java.util.ArrayList;
import java.util.List;

public class AddSongsToPlaylistDialog extends DialogFragment {

    private static final String TAG = AddSongsToPlaylistDialog.class.getName();

    public static final String MUSIC_POS = "music_pos";

    private int position;
    private List<SongData> songInfoList;
    private List<String> songInfoTemp;
    private List<Integer> selectedSongs;
    private PlaylistData playlistData;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get the playlist data
        playlistData = ((BaseActivity) getActivity()).getPlaylistInfoList().get(position);

        // Get the list of songs to pick from
        songInfoList = ((BaseActivity) getActivity()).getSongInfoList();
        songInfoTemp = new ArrayList<>();
        for (int i = 0; i < songInfoList.size(); i++) {
            // Avoid duplicates
            if (!playlistData.getSongList().contains(songInfoList.get(i))) {
                songInfoTemp.add(songInfoList.get(i).getSongName());
                Log.d(TAG, "Adding song to list.");
            } else {
                Log.d(TAG, "Song already exists.");
            }
        }

        String[] songList = new String[songInfoTemp.size()];
        songList = songInfoTemp.toArray(songList);
        selectedSongs = new ArrayList<>();

        // Get the correct position
        position = (int) getArguments().get(MUSIC_POS);
        Log.d(TAG, "Position: " + position);

        AlertDialog.Builder addSongsDialogBuilder = new AlertDialog.Builder(getActivity());
        addSongsDialogBuilder
                .setTitle(R.string.playlist_add_songs_title)
                .setMultiChoiceItems(songList, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            selectedSongs.add(which);
                            Log.d(TAG, "Adding position " + which);
                        } else {
                            selectedSongs.remove(Integer.valueOf(which));
                            Log.d(TAG, "Removing position " + which);
                        }
                    }
                })
                .setPositiveButton(R.string.song_to_playlist_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addSongsToPlaylist();
                        ((BaseActivity) getActivity()).setMainView();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Canceled adding...");
                    }
                });

        return addSongsDialogBuilder.create();
    }

    private void addSongsToPlaylist() {

        // Change the integers to songs
        List<SongData> songDataList = new ArrayList<>();
        for (Integer integer: selectedSongs) {
            songDataList.add(songInfoList.get(integer));
        }

        // Add the songs
        PlaylistUtil playlistUtil = new PlaylistUtil(getActivity());
        playlistUtil.addSongsToPlaylist(songDataList, playlistData);
    }
}
