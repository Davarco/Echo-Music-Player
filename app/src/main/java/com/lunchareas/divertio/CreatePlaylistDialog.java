package com.lunchareas.divertio;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;

public class CreatePlaylistDialog extends DialogFragment {

    private View createPlaylistView;
    private View createPlaylistTitle;
    private ArrayList<String> songInfoTemp;
    private ArrayList<Integer> selectedSongs;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // get the list of songs to pick from
        ArrayList<SongData> songInfoList = ((PlaylistActivity)getActivity()).getSongInfoList();
        songInfoTemp = new ArrayList<>();
        for (int i = 0; i < songInfoList.size(); i++) {
            songInfoTemp.add(songInfoList.get(i).getSongName());
        }

        String[] songList = new String[songInfoTemp.size()];
        songList = songInfoTemp.toArray(songList);

        // get name of playlist and list of songs
        selectedSongs = new ArrayList<>();
        AlertDialog.Builder createPlaylistBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        createPlaylistView = inflater.inflate(R.layout.create_playlist_dialog, null);
        createPlaylistTitle = inflater.inflate(R.layout.create_playlist_title, null);
        createPlaylistBuilder
                .setView(createPlaylistView)
                .setCustomTitle(createPlaylistTitle)
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
                .setPositiveButton(R.string.create_playlist_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        for (int i = 0; i < selectedSongs.size(); i++) {

                        }
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        System.out.println("Canceled creation...");
                    }
                });

        return createPlaylistBuilder.create();
    }
}
