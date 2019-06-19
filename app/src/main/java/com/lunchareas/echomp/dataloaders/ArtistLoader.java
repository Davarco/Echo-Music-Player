package com.lunchareas.echomp.dataloaders;


import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.lunchareas.echomp.models.Artist;

import java.util.ArrayList;
import java.util.List;

public class ArtistLoader {

    private static final String TAG = ArtistLoader.class.getName();

    public static List<Artist> artistList;

    public static Artist findArtistById(long id) {
        for (Artist artist: artistList) {
            if (artist.getId() == id) {
                return artist;
            }
        }

        return null;
    }

    public static List<Artist> getArtistList(Context context) {

        // Search for artists
        artistList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                new String[]{"_id", "artist", "number_of_albums", "number_of_tracks"}, null, null, null);

        // Get artist data
        if (cursor != null && cursor.moveToFirst()) {
            do {

                // Get the data
                long id = cursor.getLong(0);
                String name = cursor.getString(1);
                int numAlbums = cursor.getInt(2);
                int numTracks = cursor.getInt(3);

                // Make sure songs on album exist
                Artist artist = new Artist(id, name, numAlbums, numTracks);
                artistList.add(artist);

            } while (cursor.moveToNext());
            cursor.close();
        }

        return artistList;
    }
}
