package com.lunchareas.divertio;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class DeletePlaylistDialog extends DialogFragment {

    private List<PlaylistData> playlistInfoList;
    private List<String> playlistInfoTemp;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // get playlists to remove from
        playlistInfoList = ((PlaylistActivity)getActivity()).getPlaylistInfoList();
        playlistInfoTemp = new ArrayList<>();
        for (int i = 0; i < playlistInfoList.size(); i++) {
            playlistInfoTemp.add(playlistInfoList.get(i).getPlaylistName());
        }

        String[] playlistList = new String[playlistInfoTemp.size()];
        playlistList = playlistInfoTemp.toArray(playlistList);

        // get playlists to
    }
}
