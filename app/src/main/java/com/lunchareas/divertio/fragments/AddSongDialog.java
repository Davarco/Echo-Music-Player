package com.lunchareas.divertio.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.activities.BaseActivity;
import com.lunchareas.divertio.models.SongDBHandler;
import com.lunchareas.divertio.models.SongData;
import com.lunchareas.divertio.utils.SongUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AddSongDialog extends DialogFragment {

    private static final String TAG = AddSongDialog.class.getName();

    private Activity activity;
    private ContentResolver musicResolver;
    private Uri musicUri;
    private Cursor musicCursor;
    private List<SongData> existingSongList;
    private List<SongData> songList;
    private List<Integer> selectedSongs;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get the activity
        activity = getActivity();

        // Get current songs on app
        SongDBHandler db = new SongDBHandler(activity);
        existingSongList = db.getSongDataList();
        songList = new ArrayList<>();
        selectedSongs = new ArrayList<>();

        // Get all the songs on the phone
        musicResolver = activity.getContentResolver();
        musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        musicCursor = musicResolver.query(musicUri, null, null, null, null);

        // Iterate through results
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int pathCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            do {
                try {
                    // Get data
                    String title = musicCursor.getString(titleCol);
                    String artist = musicCursor.getString(artistCol);
                    String path = musicCursor.getString(pathCol);

                    // Get cover from path
                    Log.d(TAG, "Path: " + path);
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(path);
                    byte[] bytes = retriever.getEmbeddedPicture();
                    Drawable cover;
                    if (bytes == null) {
                        cover = getResources().getDrawable(R.drawable.default_song_icon);
                    } else {
                        cover = Drawable.createFromStream(new ByteArrayInputStream(bytes), null);
                    }

                    // Create the song data
                    SongData songData = new SongData(title, path, artist, cover);
                    Log.d(TAG, songData.toString());

                    // Ensure it doesn't already exist
                    if (!existingSongList.contains(songData)) {
                        songList.add(songData);
                    }
                } catch (Exception ignored) {}

            } while (musicCursor.moveToNext());
        }

        // List the songs
        //Log.d(TAG, "Possible songs to add: " + songList.toString());

        // Get the names of the songs
        List<String> songNameList = new ArrayList<>();
        for (SongData songData: songList) {
            songNameList.add(songData.getSongName());
        }
        String[] songNameArr = new String[songNameList.size()];
        songNameArr = songNameList.toArray(songNameArr);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.add_songs_dialog_title);
        builder.setMultiChoiceItems(songNameArr, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            selectedSongs.add(which);
                            Log.d(TAG, "Adding position " + which);
                        } else {
                            selectedSongs.remove(Integer.valueOf(which));
                            Log.d(TAG, "Removing position " + which);
                        }
                    }
                });
        builder.setPositiveButton(R.string.overflow_add_title, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addSelectedSongs();
                        ((BaseActivity) activity).setMainView();
                    }
                });
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Canceled adding...");
                    }
                });

        return builder.create();
    }

    private void addSelectedSongs() {

        // Get songs from indexes
        List<SongData> songListTemp = new ArrayList<>();
        for (Integer integer: selectedSongs) {
            songListTemp.add(songList.get(integer));
        }

        // Add from util
        SongUtil songUtil = new SongUtil(activity);
        songUtil.addSongList(songListTemp);
    }
}
