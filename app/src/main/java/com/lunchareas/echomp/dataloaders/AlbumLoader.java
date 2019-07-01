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
