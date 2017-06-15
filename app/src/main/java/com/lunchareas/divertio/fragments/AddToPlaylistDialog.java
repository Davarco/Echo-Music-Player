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
import com.lunchareas.divertio.models.SongDBHandler;
import com.lunchareas.divertio.models.SongData;
import com.lunchareas.divertio.utils.PlaylistUtil;

import java.util.ArrayList;
import java.util.List;

public class AddToPlaylistDialog extends DialogFragment {

    private static final String TAG = AddToPlaylistDialog.class.getName();

    private static final String NOT_FOUND = "FAIL";

    public static final String MUSIC_POS = "music_pos";
    public static final String MUSIC_LIST = "music_list";

    private String name;
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
            }
        }

        String[] playlistList = new String[playlistInfoTemp.size()];
        playlistList = playlistInfoTemp.toArray(playlistList);
        selectedPlaylists = new ArrayList<>();

        // Get correct name
        if (getArguments().containsKey(MUSIC_POS)) {
            name = (String) getArguments().get(MUSIC_POS);
        } else {
            name = NOT_FOUND;
        }

        // Or get the list of songs
        if (getArguments().containsKey(MUSIC_LIST)) {
            songPosList = (List<Integer>) getArguments().get(MUSIC_LIST);
        } else {
            songPosList = null;
        }

        AlertDialog.Builder addSongDialogBuilder = new AlertDialog.Builder(getActivity());
        addSongDialogBuilder
                .setCustomTitle(getActivity().getLayoutInflater().inflate(R.layout.title_add_to_playlist, null))
                .setMultiChoiceItems(playlistList, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            selectedPlaylists.add(which);
                        } else {
                            selectedPlaylists.remove(Integer.valueOf(which));
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
                    public void onClick(DialogInterface dialog, int which) {}
                });

        return addSongDialogBuilder.create();
    }

    private void addToPlaylists() {

        // Get the song list
        List<SongData> baseSongList = ((BaseActivity) getActivity()).getSongInfoList();

        // Position exists
        if (name != NOT_FOUND) {
            songData = new SongDBHandler(getActivity()).getSongData(name);

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
            for (Integer idx: selectedPlaylists) {
                PlaylistData playlistData = playlistInfoList.get(idx);
                playlistUtil.addSongsToPlaylist(songList, playlistData);
                playlistUtil.removeDuplicateSongs(playlistData);
            }
        }
    }
}
