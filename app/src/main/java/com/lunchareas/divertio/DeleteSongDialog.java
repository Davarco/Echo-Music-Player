package com.lunchareas.divertio;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DeleteSongDialog extends DialogFragment {

    private List<SongData> songInfoList;
    private List<Integer> selectedSongs;
    private List<String> songInfoTemp;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // get the list of songs to pick from
        songInfoList = ((MainActivity)getActivity()).getSongInfoList();
        songInfoTemp = new ArrayList<>();
        for (int i = 0; i < songInfoList.size(); i++) {
            songInfoTemp.add(songInfoList.get(i).getSongName());
        }

        String[] songList = new String[songInfoTemp.size()];
        songList = songInfoTemp.toArray(songList);

        // get songs to delete with multiple choice boxes
        selectedSongs = new ArrayList<>();
        AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(getActivity());
        deleteBuilder
            .setTitle(R.string.delete_dialog_title)
            .setMultiChoiceItems(songList, null, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                    if (isChecked) {
                        selectedSongs.add(position);
                        System.out.println("Adding position " + position);
                    } else if (selectedSongs.contains(position)) {
                        selectedSongs.remove(Integer.valueOf(position));
                        System.out.println("Removing position " + position);
                    }
                }
            })
            .setPositiveButton(R.string.delete_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    for (int i = 0; i < selectedSongs.size(); i++) {

                        // delete from database
                        SongData songData = songInfoList.get(selectedSongs.get(i));
                        SongDBHandler db = new SongDBHandler(getActivity());
                        new File(db.getSongData(songData.getSongName()).getSongPath()).delete();
                        db.deleteSongData(songData);
                    }
                    ((MainActivity)getActivity()).setSongListView();
                }
            })
            .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    System.out.println("Canceled deletion...");
                }
            });

        return deleteBuilder.create();
    }
}
