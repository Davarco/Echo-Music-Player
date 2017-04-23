package com.lunchareas.divertio.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.lunchareas.divertio.activities.BaseActivity;
import com.lunchareas.divertio.models.PlaylistDBHandler;
import com.lunchareas.divertio.models.PlaylistData;
import com.lunchareas.divertio.R;
import com.lunchareas.divertio.activities.PlaylistActivity;

import java.util.ArrayList;
import java.util.List;

public class DeletePlaylistDialog extends DialogFragment {

    private static final String TAG = DeletePlaylistDialog.class.getName();

    private List<PlaylistData> playlistInfoList;
    private List<String> playlistInfoTemp;
    private List<Integer> selectedPlaylists;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get playlists to remove from
        playlistInfoList = ((PlaylistActivity)getActivity()).getPlaylistInfoList();
        playlistInfoTemp = new ArrayList<>();
        for (int i = 0; i < playlistInfoList.size(); i++) {
            playlistInfoTemp.add(playlistInfoList.get(i).getPlaylistName());
        }

        String[] playlistList = new String[playlistInfoTemp.size()];
        playlistList = playlistInfoTemp.toArray(playlistList);

        // Get playlists to delete
        selectedPlaylists = new ArrayList<>();
        AlertDialog.Builder deletePlaylistBuilder = new AlertDialog.Builder(getActivity());
        deletePlaylistBuilder
                .setTitle(R.string.delete_playlist_title)
                .setMultiChoiceItems(playlistList, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                        if (isChecked) {
                            selectedPlaylists.add(position);
                            Log.d(TAG, "Adding position " + position);
                        } else {
                            selectedPlaylists.remove(Integer.valueOf(position));
                            Log.d(TAG, "Removing position " + position);
                        }
                    }
                })
                .setPositiveButton(R.string.delete_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        for (int i = 0; i < selectedPlaylists.size(); i++) {

                            // Delete from database
                            PlaylistData playlistData = playlistInfoList.get(selectedPlaylists.get(i));
                            PlaylistDBHandler db = new PlaylistDBHandler(getActivity());
                            db.deletePlaylistData(playlistData);
                        }
                        ((BaseActivity) getActivity()).setMainView();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "Canceled deletion...");
                    }
                });

        return deletePlaylistBuilder.create();
    }
}
