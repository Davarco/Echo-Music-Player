/*
Echo Music Player
Copyright (C) 2019 David Zhang

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
