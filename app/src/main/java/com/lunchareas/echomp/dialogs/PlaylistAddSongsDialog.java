package com.lunchareas.echomp.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.lunchareas.echomp.R;
import com.lunchareas.echomp.activities.MainActivity;
import com.lunchareas.echomp.dataloaders.PlaylistLoader;
import com.lunchareas.echomp.models.Playlist;
import com.lunchareas.echomp.models.Song;
import com.lunchareas.echomp.utils.Constants;
import com.lunchareas.echomp.utils.MediaDataUtils;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAddSongsDialog extends DialogFragment {

    private List<Playlist> playlistList;
    private List<Playlist> temp;
    private String[] playlistNames;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get playlists
        temp = new ArrayList<>();
        playlistList = PlaylistLoader.getPlaylistList(getActivity());
        for (int i = 0; i < playlistList.size(); i++) {
            if (playlistList.get(i).getId() >= 0) {
                temp.add(playlistList.get(i));
            }
        }
        playlistNames = new String[temp.size()];
        for (int i = 0; i < temp.size(); i++) {
            playlistNames[i] = temp.get(i).getName();
        }

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCustomTitle(View.inflate(getActivity(), R.layout.title_playlist_add_songs, null));
        builder.setSingleChoiceItems(playlistNames, 0, null);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selected = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                if (temp.size() > 0 && temp.get(selected) != null)
                    MediaDataUtils.addSongsToPlaylist(temp.get(selected).getId(),
                        getArguments().<Song> getParcelableArrayList(Constants.SONG_LIST), getActivity());
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).updateAll();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });

        return builder.create();
    }
}
