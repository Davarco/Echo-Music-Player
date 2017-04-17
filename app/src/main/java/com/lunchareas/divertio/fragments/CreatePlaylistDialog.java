package com.lunchareas.divertio.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.lunchareas.divertio.models.PlaylistDBHandler;
import com.lunchareas.divertio.models.PlaylistData;
import com.lunchareas.divertio.R;
import com.lunchareas.divertio.models.SongData;
import com.lunchareas.divertio.activities.BaseActivity;
import com.lunchareas.divertio.activities.PlaylistActivity;

import java.util.ArrayList;
import java.util.List;

public class CreatePlaylistDialog extends DialogFragment {

    private static final String TAG = CreatePlaylistDialog.class.getName();

    private View createPlaylistView;
    private View createPlaylistTitle;
    private List<String> songInfoTemp;
    private List<Integer> selectedSongs;
    private List<SongData> songInfoList;
    private EditText playlistNameInput;
    private String playlistName;
    private Activity activity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // get the list of songs to pick from
        activity = getActivity();
        songInfoList = ((BaseActivity)getActivity()).getSongInfoList();
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
        
        // parts of the dialog
        createPlaylistBuilder.setCustomTitle(createPlaylistTitle);
        createPlaylistBuilder.setMultiChoiceItems(songList, null, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                    if (isChecked) {
                        selectedSongs.add(position);
                        Log.d(TAG, "Adding position " + position);
                    } else if (selectedSongs.contains(position)) {
                        selectedSongs.remove(Integer.valueOf(position));
                        Log.d(TAG, "Removing position " + position);
                    }
                }
            });
        createPlaylistBuilder.setPositiveButton(R.string.create_playlist_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    getPlaylistName();
                }
            });
        createPlaylistBuilder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    Log.d(TAG, "Canceled creation...");
                }
            });

        return createPlaylistBuilder.create();
    }
    
    private void getPlaylistName() {
        
        // Create another dialog to get the name
        AlertDialog.Builder nameDialogBuilder = new AlertDialog.Builder(getActivity());
        nameDialogBuilder.setView(createPlaylistView);
        nameDialogBuilder.setPositiveButton(R.string.create_playlist_finish, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // get name from input
                playlistNameInput = (EditText) createPlaylistView.findViewById(R.id.dialog_create_playlist_name);
                playlistName = playlistNameInput.getText().toString().trim();
                Log.d(TAG, "Playlist name: " + playlistName);
                executePlaylistCreate();
            }
        });
        nameDialogBuilder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "Canceled creation...");
            }
        });
        AlertDialog nameDialog = nameDialogBuilder.create();
        nameDialog.show();
    }

    private void executePlaylistCreate() {

        // create a list of the song data
        List<SongData> songDataList = new ArrayList<>();
        for (int i = 0; i < selectedSongs.size(); i++) {
            SongData songData = songInfoList.get(selectedSongs.get(i));
            songDataList.add(songData);
            Log.i(TAG, "song name: " + songDataList.get(i).getSongName() + ", song artist: " + songDataList.get(i).getSongArtist());
        }

        // update database with new playlist
        PlaylistDBHandler db = new PlaylistDBHandler(activity);
        try {
            PlaylistData playlistData = new PlaylistData(playlistName, songDataList);
            db.addPlaylistData(playlistData);
            Log.d(TAG, "Successfully updated playlist database.");
        } catch (Exception e) {
            Log.e(TAG, "Playlist database update failure.", e);
        }

        ((PlaylistActivity) activity).setPlaylistView();
    }
}
