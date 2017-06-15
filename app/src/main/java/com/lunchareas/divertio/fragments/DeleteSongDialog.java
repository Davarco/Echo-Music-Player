package com.lunchareas.divertio.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.activities.BaseActivity;
import com.lunchareas.divertio.activities.BaseListActivity;
import com.lunchareas.divertio.models.SongData;
import com.lunchareas.divertio.activities.MainActivity;
import com.lunchareas.divertio.utils.SongUtil;

import java.util.ArrayList;
import java.util.List;

public class DeleteSongDialog extends DialogFragment {

    private static final String TAG = DeleteSongDialog.class.getName();

    private List<SongData> songInfoList;
    private List<Integer> selectedSongs;
    private List<String> songInfoTemp;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get the list of songs to pick from
        songInfoList = ((BaseActivity) getActivity()).getSongInfoList();
        songInfoTemp = new ArrayList<>();
        for (int i = 0; i < songInfoList.size(); i++) {
            songInfoTemp.add(songInfoList.get(i).getSongName());
        }

        String[] songList = new String[songInfoTemp.size()];
        songList = songInfoTemp.toArray(songList);

        // Get songs to delete with multiple choice boxes
        selectedSongs = new ArrayList<>();
        AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(getActivity());
        deleteBuilder
            .setCustomTitle(getActivity().getLayoutInflater().inflate(R.layout.title_delete_song, null))
            .setMultiChoiceItems(songList, null, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                    if (isChecked) {
                        selectedSongs.add(position);
                    } else if (selectedSongs.contains(position)) {
                        selectedSongs.remove(Integer.valueOf(position));
                    }
                }
            })
            .setPositiveButton(R.string.delete_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    for (int i = 0; i < selectedSongs.size(); i++) {

                        // Delete from database
                        SongData songData = songInfoList.get(selectedSongs.get(i));
                        SongUtil songController = new SongUtil(getActivity());
                        songController.deleteSong(songData);
                    }
                    ((BaseActivity) getActivity()).setMainView();
                }
            })
            .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {}
            });

        return deleteBuilder.create();
    }
}
