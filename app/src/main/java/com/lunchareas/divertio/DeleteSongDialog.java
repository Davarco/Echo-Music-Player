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

public class DeleteSongDialog extends DialogFragment {

    private ArrayList<Integer> selectedSongs;
    private ArrayList<String> songInfoTemp;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // get the list of songs to pick from
        ArrayList<SongData> songInfoList = ((MainActivity)getActivity()).getSongInfoList();
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

                        // get file name to delete and open both dir with music files
                        String songName = songInfoTemp.get(selectedSongs.get(i));
                        File musicFolder = new File(Environment.getExternalStorageDirectory() + File.separator + "Divertio");
                        File musicInfoFolder = getActivity().getApplicationContext().getDir("DivertioInfoFiles", Context.MODE_PRIVATE);
                        File musicInfoLister = musicInfoFolder.getAbsoluteFile();

                        // find the mp3 file and delete it
                        System.out.println("Looking for song: " + songName);
                        for (File songFile: musicFolder.listFiles()) {
                            System.out.println("File name: " + songFile.getName());
                            if (songFile.getName().equals(songName+".mp3")) {
                                System.out.println("Found file!");
                                if (songFile.delete()) {
                                    System.out.println("Deleted file successfully!");
                                } else {
                                    System.out.println("File deletion was unsuccessful!");
                                }
                            }
                        }

                        // find the config file and delete it
                        System.out.println("Looking for song file: " + songName);
                        for (File songInfoFile: musicInfoLister.listFiles()) {
                            System.out.println("Info file name: " + songInfoFile.getName());
                            if (songInfoFile.getName().equals(songName+".txt")) {
                                System.out.println("Found file!");
                                if (songInfoFile.delete()) {
                                    System.out.println("Deleted file successfully!");
                                } else {
                                    System.out.println("File deletion was unsuccessful!");
                                }
                            }
                        }
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
