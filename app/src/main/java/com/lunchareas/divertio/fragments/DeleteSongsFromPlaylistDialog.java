package com.lunchareas.divertio.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.activities.BaseActivity;
import com.lunchareas.divertio.models.PlaylistDBHandler;
import com.lunchareas.divertio.models.SongDBHandler;
import com.lunchareas.divertio.models.SongData;
import com.lunchareas.divertio.utils.PlaylistUtil;

import java.util.ArrayList;
import java.util.List;

public class DeleteSongsFromPlaylistDialog extends DialogFragment {

    private static final String TAG = DeleteSongsFromPlaylistDialog.class.getName();

    public static final String MUSIC_POS = "music_pos";

    private String name;
    private List<SongData> songInfoList;
    private List<String> songInfoTemp;
    private List<Integer> selectedSongs;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get the correct name
        name = (String) getArguments().get(MUSIC_POS);

        // Get the list of songs to pick from
        songInfoList = new PlaylistDBHandler(getActivity()).getPlaylistData(name).getSongList();
        songInfoTemp = new ArrayList<>();
        for (int i = 0; i < songInfoList.size(); i++) {
            songInfoTemp.add(songInfoList.get(i).getSongName());
        }

        String[] songList = new String[songInfoTemp.size()];
        songList = songInfoTemp.toArray(songList);
        selectedSongs = new ArrayList<>();

        AlertDialog.Builder addSongsDialogBuilder = new AlertDialog.Builder(getActivity());
        addSongsDialogBuilder
                .setCustomTitle(getActivity().getLayoutInflater().inflate(R.layout.title_delete_songs_from_playlist, null))
                .setMultiChoiceItems(songList, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            selectedSongs.add(which);
                        } else {
                            selectedSongs.remove(Integer.valueOf(which));
                        }
                    }
                })
                .setPositiveButton(R.string.song_to_playlist_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeSongsFromPlaylist();
                        ((BaseActivity) getActivity()).setMainView();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });

        return addSongsDialogBuilder.create();
    }

    private void removeSongsFromPlaylist() {

        // Handlers
        PlaylistDBHandler playlistDb = new PlaylistDBHandler(getActivity());

        // Change the integers to songs
        List<SongData> songDataList = new ArrayList<>();
        for (Integer integer: selectedSongs) {
            songDataList.add(songInfoList.get(integer));
        }

        // Add the songs
        PlaylistUtil playlistUtil = new PlaylistUtil(getActivity());
        playlistUtil.deleteSongsFromPlaylist(songDataList, playlistDb.getPlaylistData(name));
    }
}
