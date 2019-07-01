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


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.lunchareas.echomp.models.Playlist;
import com.lunchareas.echomp.utils.Constants;

import java.util.ArrayList;
import java.util.List;


public class PlaylistLoader {

    private static final String TAG = PlaylistLoader.class.getName();

    private static final String MUSIC_ONLY_SELECTION = MediaStore.Audio.AudioColumns.IS_MUSIC + "=1"
            + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''";

    public static List<Playlist> playlistList;

    public static Playlist findPlaylistById(long id) {
        for (Playlist playlist: playlistList) {
            if (playlist.getId() == id) {
                return playlist;
            }
        }

        return null;
    }

    public static List<Playlist> getPlaylistList(Context context) {

        // Add default playlists
        playlistList = new ArrayList<>();
        final Playlist lastAdded = new Playlist(Constants.LAST_ADDED_ID, "Last Added", -1);
        final Playlist recentlyPlayed = new Playlist(Constants.RECENTLY_PLAYED_ID, "Recently Played", -1);
        final Playlist topTracks = new Playlist(Constants.TOP_TRACKS_ID, "Top Tracks", -1);
        playlistList.add(lastAdded);
        playlistList.add(recentlyPlayed);
        playlistList.add(topTracks);

        // Search for playlists
        Uri playlistUri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(playlistUri, new String[]{ BaseColumns._ID, MediaStore.Audio.PlaylistsColumns.NAME },
                null, null, null);

        // Iterate through data
        if (cursor != null && cursor.moveToFirst()) {
            int idCol = cursor.getColumnIndex(MediaStore.Audio.Playlists._ID);
            int nameCol = cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME);

            do {

                // Get data
                long id = cursor.getLong(idCol);
                String name = cursor.getString(nameCol);
                int count = 0;

                // Get count
                Cursor c = contentResolver.query(MediaStore.Audio.Playlists.Members.getContentUri("external", id),
                        new String[]{BaseColumns._ID}, MUSIC_ONLY_SELECTION, null, null);
                if (c != null) {
                    if (c.moveToFirst()) {
                        count = c.getCount();
                    }

                    c.close();
                }

                // Create model and add to list
                playlistList.add(new Playlist(id, name, count));

            } while (cursor.moveToNext());
            cursor.close();
        }

        return playlistList;
    }
}
