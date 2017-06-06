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

    private static final int NOT_FOUND = -1;

    public static final String MUSIC_POS = "music_pos";
    public static final String MUSIC_LIST = "music_list";

    private int position;
    private List<Integer> songPosList;
    private List<PlaylistData> playlistInfoList;
    private List<String> playlistInfoTemp;
    private List<Integer> selectedPlaylists;
    private SongData songData;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get the list of playlists to pick from
        playlistInfoList = ((BaseActivity) getActivity()).getPlaylistInfoList();
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
        if (getArguments().containsKey(MUSIC_POS)) {
            position = (int) getArguments().get(MUSIC_POS);
            Log.d(TAG, "Position: " + position);
        } else {
            position = NOT_FOUND;
        }

        // Or get the list of songs
        if (getArguments().containsKey(MUSIC_LIST)) {
            songPosList = (List<Integer>) getArguments().get(MUSIC_LIST);
        } else {
            songPosList = null;
            Log.d(TAG, "Song list passed was null.");
        }

        AlertDialog.Builder addSongDialogBuilder = new AlertDialog.Builder(getActivity());
        addSongDialogBuilder.setTitle(R.string.song_to_playlist_title);
        addSongDialogBuilder.setMultiChoiceItems(playlistList, null, new DialogInterface.OnMultiChoiceClickListener() {
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
                });
        addSongDialogBuilder.setPositiveButton(R.string.song_to_playlist_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addToPlaylists();
                    }
                });
        addSongDialogBuilder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Canceled adding to playlist...");
                    }
                });

        return addSongDialogBuilder.create();
    }

    private void addToPlaylists() {

        // Get the song list
        List<SongData> baseSongList = ((BaseActivity) getActivity()).getSongInfoList();

        // Position exists
        if (position != NOT_FOUND) {
            songData = baseSongList.get(position);

            // Add song to playlists
            PlaylistUtil playlistUtil = new PlaylistUtil(getActivity());
            for (Integer idx : selectedPlaylists) {
                PlaylistData playlistData = playlistInfoList.get(idx);
                playlistUtil.addSongToPlaylist(songData, playlistData);
            }
        }

        // Song list exists
        if (songPosList != null) {
            List<SongData> songList = new ArrayList<>();
            for (Integer integer: songPosList) {
                songList.add(baseSongList.get(integer));
            }

            // Add list of songs
            PlaylistUtil playlistUtil = new PlaylistUtil(getActivity());
            for (Integer idx : selectedPlaylists) {
                Log.d(TAG, "Adding songs to playlist " + playlistInfoList.get(idx).getPlaylistName());
                PlaylistData playlistData = playlistInfoList.get(idx);
                playlistUtil.addSongsToPlaylist(songList, playlistData);
                playlistUtil.removeDuplicateSongs(playlistData);
            }
        }
    }
}
