package com.lunchareas.divertio.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImportSongDialog extends DialogFragment {

    private static final String TAG = ImportSongDialog.class.getName();

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
            int albumIdCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

            do {
                // Get data
                String title = musicCursor.getString(titleCol);
                String artist = musicCursor.getString(artistCol);
                String path = musicCursor.getString(pathCol);
                long albumId = musicCursor.getLong(albumIdCol);

                // Get path for cover art
                String coverPath = getCoverArtPath(albumId, getContext());
                Drawable cover = Drawable.createFromPath(coverPath);

                // Get cover from path
                /*
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                Drawable cover = new BitmapDrawable(getContext().getResources(), bitmap);
                */
                /*
                retriever.setDataSource(getContext(), uri);
                byte[] bytes = retriever.getEmbeddedPicture();
                Drawable cover;
                if (bytes == null) {
                    cover = getResources().getDrawable(R.drawable.ic_media_icon);
                } else {
                    cover = Drawable.createFromStream(new ByteArrayInputStream(bytes), null);
                }
                */

                // Set default icon if needed
                if (cover == null) {
                    cover = getResources().getDrawable(R.drawable.default_cover);
                }

                // Create the song data
                if (cover != null) {
                    SongData songData = new SongData(title, path, artist, cover);

                    // Ensure it doesn't already exist
                    if (!existingSongList.contains(songData)) {
                        songList.add(songData);
                    }
                } else {
                    Log.d(TAG, "Empty, not being considered. " + title + " " + path);
                }

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
        builder
                .setCustomTitle(getActivity().getLayoutInflater().inflate(R.layout.title_import_song, null))
                .setMultiChoiceItems(songNameArr, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            selectedSongs.add(which);
                            // Log.d(TAG, "Adding position " + which);
                        } else {
                            selectedSongs.remove(Integer.valueOf(which));
                            // Log.d(TAG, "Removing position " + which);
                        }
                    }
                })
                .setPositiveButton(R.string.overflow_add_title, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addSelectedSongs();
                        ((BaseActivity) activity).setMainView();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Log.d(TAG, "Canceled adding...");
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

    private static String getCoverArtPath(long albumId, Context context) {
        Cursor albumCursor = context.getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + " = ?",
                new String[]{Long.toString(albumId)},
                null
        );
        boolean queryResult = albumCursor.moveToFirst();
        String result = null;
        if (queryResult) {
            result = albumCursor.getString(0);
        }
        albumCursor.close();
        return result;
    }
}
