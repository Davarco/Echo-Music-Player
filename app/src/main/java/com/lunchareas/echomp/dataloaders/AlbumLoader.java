package com.lunchareas.echomp.dataloaders;


import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.lunchareas.echomp.models.Album;

import java.util.ArrayList;
import java.util.List;

public class AlbumLoader {

    private static final String TAG = AlbumLoader.class.getName();

    public static List<Album> albumList;

    public static Album findAlbumById(long id) {
        for (Album album: albumList) {
            if (album.getId() == id) {
                return album;
            }
        }

        return null;
    }

    public static List<Album> getAlbumList(Context context) {

        // Search for albums
        albumList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{"_id", "album", "artist", "artist_id", "numsongs"}, null, null, null);

        // Get album data
        if (cursor != null && cursor.moveToFirst()) {
            do {

                // Get the data
                long id = cursor.getLong(0);
                String name = cursor.getString(1);
                String artist = cursor.getString(2);
                long artistId = cursor.getLong(3);
                int count = cursor.getInt(4);

                // Create model and add to list
                Album album = new Album(id, name, artist, artistId, count);
                albumList.add(album);

            } while (cursor.moveToNext());
            cursor.close();
        }

        return albumList;
    }
}
