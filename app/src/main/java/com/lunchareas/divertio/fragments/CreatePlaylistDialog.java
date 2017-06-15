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
import com.lunchareas.divertio.activities.PlaylistMenuActivity;
import com.lunchareas.divertio.utils.PlaylistUtil;

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
    private PlaylistUtil playlistUtil;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Create the playlist util
        playlistUtil = new PlaylistUtil(getActivity());

        // Get the list of songs to pick from
        activity = getActivity();
        songInfoList = ((BaseActivity)getActivity()).getSongInfoList();
        songInfoTemp = new ArrayList<>();
        for (int i = 0; i < songInfoList.size(); i++) {
            songInfoTemp.add(songInfoList.get(i).getSongName());
        }

        String[] songList = new String[songInfoTemp.size()];
        songList = songInfoTemp.toArray(songList);

        // Get name of playlist and list of songs
        selectedSongs = new ArrayList<>();
        AlertDialog.Builder createPlaylistBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        createPlaylistView = inflater.inflate(R.layout.dialog_create_playlist, null);
        createPlaylistTitle = inflater.inflate(R.layout.title_create_playlist, null);
        
        // Parts of the dialog
        createPlaylistBuilder.setCustomTitle(createPlaylistTitle);
        createPlaylistBuilder.setMultiChoiceItems(songList, null, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                    if (isChecked) {
                        selectedSongs.add(position);
                    } else if (selectedSongs.contains(position)) {
                        selectedSongs.remove(Integer.valueOf(position));
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
                public void onClick(DialogInterface dialogInterface, int which) {}
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

                // Get name from input
                playlistNameInput = (EditText) createPlaylistView.findViewById(R.id.dialog_create_playlist_name);
                playlistName = playlistNameInput.getText().toString().trim();
                if (!playlistUtil.nameAlreadyExists(playlistName)) {
                    executePlaylistCreate();
                } else {

                    // Create dialog because name is invalid
                    ((PlaylistMenuActivity) activity).createPlaylistNameFailureDialog();
                }
            }
        });
        nameDialogBuilder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {}
        });
        AlertDialog nameDialog = nameDialogBuilder.create();
        nameDialog.show();
    }

    private void executePlaylistCreate() {

        // Create a list of the song data
        List<SongData> songDataList = new ArrayList<>();
        for (int i = 0; i < selectedSongs.size(); i++) {
            SongData songData = songInfoList.get(selectedSongs.get(i));
            songDataList.add(songData);
        }

        // Update database with new playlist
        PlaylistDBHandler db = new PlaylistDBHandler(activity);
        try {
            PlaylistData playlistData = new PlaylistData(playlistName, songDataList);
            db.addPlaylistData(playlistData);
        } catch (Exception e) {
            Log.e(TAG, "Playlist database update failure.", e);
        }

        ((BaseActivity) activity).setMainView();
    }
}
