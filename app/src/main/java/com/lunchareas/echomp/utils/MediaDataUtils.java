package com.lunchareas.echomp.utils;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lunchareas.echomp.R;
import com.lunchareas.echomp.databases.RecentlyPlayedPair;
import com.lunchareas.echomp.databases.RecentlyPlayedStore;
import com.lunchareas.echomp.databases.TopTracksPair;
import com.lunchareas.echomp.databases.TopTracksStore;
import com.lunchareas.echomp.dataloaders.SongLoader;
import com.lunchareas.echomp.models.Song;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MediaDataUtils {

    private static final String TAG = MediaDataUtils.class.getName();

    private static final int NUM_OF_DAYS = 7;

    /*
    SONG UTILITIES:
     */
    public static void deleteSong(long id, String path, Context context) {

        // Delete the file
        File file = new File(path);
        if (!file.delete()) {
            Log.e(TAG, "Failed to delete song?");
        }

        // Delete song from db
        context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "_id=" + id, null);
    }

    public static void deleteSongList(List<Song> songList, Context context) {

        // Go through songs and delete individually
        for (Song song: songList) {

            // Delete the file
            File file = new File(song.getPath());
            if (!file.delete()) {
                Log.e(TAG, "Failed to delete song?");
            }

            // Delete song from db
            context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "_id=" + song.getId(), null);
        }
    }

    public static void renameSong(long id, String name, Context context) {

        // Create values
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.TITLE, name);

        // Update database
        context.getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values, "_id=" + id, null);
    }

    public static void changeSongAlbum(long id, String album, Context context) {

        // Create values
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.ALBUM, album);

        // Update database
        context.getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values, "_id=" + id, null);
    }

    public static void changeSongArtist(long id, String artist, Context context) {

        // Create values
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.ARTIST, artist);

        // Update database
        context.getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values, "_id=" + id, null);
    }

    public static void updateSongMetadata(final long songId, final long albumId, final String url, final String path, final Context context) {

        // Get youtube ID and JSON object
        final String videoId = MusicDownloadUtils.getYoutubeId(url);

        /*
        Update cover art from YT
         */
        Thread image = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    // Connect to URL
                    Log.e(TAG, "Connecting to YT API for data.");
                    URL jsonUrl = new URL("https://www.googleapis.com/youtube/v3/videos?id=" + videoId +
                            "&key=" + Constants.YT_API_KEY + "&part=snippet");
                    HttpURLConnection request = (HttpURLConnection) jsonUrl.openConnection();
                    request.connect();

                    // Convert to json object
                    JsonParser parser = new JsonParser();
                    JsonElement root = parser.parse(new InputStreamReader((InputStream) request.getContent()));
                    JsonObject object = root.getAsJsonObject();
                    JsonObject thumbnails = object.getAsJsonArray("items").get(0).getAsJsonObject().getAsJsonObject("snippet").getAsJsonObject("thumbnails");

                    // Change artist name
                    boolean standard = thumbnails.has("standard");
                    boolean maxres = thumbnails.has("maxres");

                    // Download highest quality image
                    if (maxres) {
                        String link = thumbnails.getAsJsonObject("maxres").get("url").toString();
                        link = link.substring(1, link.length()-1);
                        ImageDownloadUtils.downloadSongArt(link, songId, albumId, context);
                        Log.e(TAG, "Downloading max res image.");
                    } else if (standard) {
                        String link = thumbnails.getAsJsonObject("standard").get("url").toString();
                        link = link.substring(1, link.length()-1);
                        ImageDownloadUtils.downloadSongArt(link, songId, albumId, context);
                        Log.e(TAG, "Downloading standard res image.");
                    } else {
                        String link = thumbnails.getAsJsonObject("high").get("url").toString();
                        link = link.substring(1, link.length()-1);
                        ImageDownloadUtils.downloadSongArt(link, songId, albumId, context);
                        Log.e(TAG, "Downloading low res image.");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        /*
        Get data from Youtube API, key in Constants.java
         */
        Thread metadata = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Connect to URL
                    URL jsonUrl = new URL("https://www.googleapis.com/youtube/v3/videos?id=" + videoId +
                            "&key=" + Constants.YT_API_KEY + "&part=snippet");
                    HttpURLConnection request = (HttpURLConnection) jsonUrl.openConnection();
                    request.connect();

                    // Convert to json object
                    JsonParser parser = new JsonParser();
                    JsonElement root = parser.parse(new InputStreamReader((InputStream) request.getContent()));
                    JsonObject object = root.getAsJsonObject();
                    JsonObject snippet = object.getAsJsonArray("items").get(0).getAsJsonObject().getAsJsonObject("snippet");

                    /*
                    for (Map.Entry<String, JsonElement> entry: artistObject.entrySet()) {
                        Log.e(TAG, entry.getKey());
                        if (entry.getKey().equals("channelTitle")) {
                            MediaDataUtils.changeSongArtist(songId, entry.getValue().toString(), context);
                            Log.e(TAG, "Changed artist to " + entry.getValue().toString() + ".");
                        }
                    }
                    */

                    // Change artist name
                    String artist = snippet.get("channelTitle").toString();
                    artist = artist.substring(1, artist.length()-1);
                    MediaDataUtils.changeSongArtist(songId, artist, context);

                    // TODO Find better album names?
                    String album = snippet.get("title").toString();
                    album = album.substring(1, album.length()-1);
                    MediaDataUtils.changeSongAlbum(songId, album, context);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Wait for completions
        metadata.start();
        image.start();
        try {
            metadata.join();
            image.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Finished waiting for image and metadata change.");
    }

    /*
    PLAYLIST UTILITIES:
     */
    public static long createPlaylist(String name, Context context) {

        // Create cursor
        ContentResolver resolver = context.getContentResolver();
        String[] projection = new String[]{ MediaStore.Audio.PlaylistsColumns.NAME };
        String selection = MediaStore.Audio.PlaylistsColumns.NAME + " = '" + name + "'";
        Cursor cursor = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                projection, selection, null, null);

        // Create playlist in media store
        if (cursor != null) {
            if (cursor.getCount() <= 0) {
                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Audio.PlaylistsColumns.NAME, name);
                Uri uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                        values);
                if (uri != null) {
                    cursor.close();
                    return Long.parseLong(uri.getLastPathSegment());
                }
            }
            cursor.close();
        }

        return 0;
    }

    public static void deletePlaylist(long id, Context context) {

        // Delete from db
        context.getContentResolver().delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, "_id=" + id, null);
    }

    public static void deletePlaylistSongs(long playlistId, List<Long> selected, Context context) {

        // Go through songs and delete
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        ContentResolver resolver = context.getContentResolver();
        for (Long id: selected) {
            String[] loc = { Long.toString(id) };
            resolver.delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID + "=?", loc);
        }
    }

    public static void renamePlaylist(long id, String name, Context context) {

        // Create the data
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.NAME, name);

        // Insert data
        context.getContentResolver().update(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values, "_id=" + id, null);
    }

    public static List<Song> getSongsFromPlaylist(long playlistId, Context context) {

        // Initial model
        List<Song> songList;

        // Handle diff cases
        if (playlistId == Constants.LAST_ADDED_ID)
            songList = getSongsFromLastAddedPlaylist();
        else if (playlistId == Constants.RECENTLY_PLAYED_ID)
            songList = getSongsFromRecentlyPlayedPlaylist(context);
        else if (playlistId == Constants.TOP_TRACKS_ID)
            songList = getSongsFromTopTracksPlaylist(context);
        else
            songList = getSongsFromDefaultPlaylist(playlistId, context);

        return songList;
    }

    private static List<Song> getSongsFromLastAddedPlaylist() {

        // Get songs first
        List<Song> songList = new ArrayList<>();
        for (Song song: SongLoader.songList) {
            if (song.getDate() > (System.currentTimeMillis()/1000 - NUM_OF_DAYS*3600*24))
                songList.add(song);
        }

        return songList;
    }

    private static List<Song> getSongsFromRecentlyPlayedPlaylist(Context context) {

        // Get songs from store
        RecentlyPlayedStore store = new RecentlyPlayedStore(context);
        List<RecentlyPlayedPair> pairList = store.getPairList();
        Collections.sort(pairList, new Comparator<RecentlyPlayedPair>() {
            public int compare(RecentlyPlayedPair pair1, RecentlyPlayedPair pair2) {
                return pair2.getVal() - pair1.getVal();
            }
        });

        // Create song list
        List<Song> songList = new ArrayList<>();
        for (int i = 0; i < pairList.size(); i++) {
            songList.add(SongLoader.findSongById(pairList.get(i).getKey()));
        }

        return songList;
    }

    private static List<Song> getSongsFromTopTracksPlaylist(Context context) {

        // Get songs from store
        TopTracksStore store = new TopTracksStore(context);
        List<TopTracksPair> pairList = store.getPairList();
        Collections.sort(pairList, new Comparator<TopTracksPair>() {
            public int compare(TopTracksPair pair1, TopTracksPair pair2) {
                return pair2.getVal() - pair1.getVal();
            }
        });

        // Get number of songs
        int num = (int) (pairList.size()*0.3);
        if (num < 3)
            num = pairList.size();
        else if (num > 6)
            num = 6;

        // Create song list
        List<Song> songList = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            songList.add(SongLoader.findSongById(pairList.get(i).getKey()));
        }

        return songList;
    }
    private static List<Song> getSongsFromDefaultPlaylist(long playlistId, Context context) {

        // Initial model
        List<Song> songList = new ArrayList<>();

        // Get data
        String[] projection = {
                MediaStore.Audio.Playlists.Members.AUDIO_ID,
                MediaStore.Audio.Playlists.Members.TITLE,
                MediaStore.Audio.Playlists.Members.DATA,
                MediaStore.Audio.Playlists.Members.ARTIST,
                MediaStore.Audio.Playlists.Members.ARTIST_ID,
                MediaStore.Audio.Playlists.Members.ALBUM,
                MediaStore.Audio.Playlists.Members.ALBUM_ID,
                MediaStore.Audio.Playlists.Members.DURATION,
                MediaStore.Audio.Playlists.Members.DATE_ADDED
        };
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId), projection, null, null, null);

        // Go through cursor
        if (cursor != null && cursor.moveToFirst()) {
            do {

                // Get the data
                long id = cursor.getLong(0);
                String title = cursor.getString(1);
                String path = cursor.getString(2);
                String artist = cursor.getString(3);
                long artistId = cursor.getLong(4);
                String album = cursor.getString(5);
                long albumId = cursor.getLong(6);
                int duration = cursor.getInt(7);
                long date = cursor.getLong(8);

                // Create media player
                MediaPlayer mp = MediaPlayer.create(context, Uri.parse(path));
                if (mp != null) {

                    // Create model and add to list
                    Song song = new Song(id, title, path, artist, artistId, album, albumId, duration, date);
                    songList.add(song);
                }

            } while (cursor.moveToNext());
            cursor.close();
        }

        return songList;
    }

    public static void addSongsToPlaylist(long id, List<Song> songList, Context context) {

        // Ignore empty songs
        if (songList == null || songList.size() == 0)
            return;

        // Create values
        int count = getCountWithPlaylistId(context, id);
        ContentValues[] values = new ContentValues[songList.size()];
        for (int i = 0; i < songList.size(); i++) {
            values[i] = new ContentValues();
            values[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, i+count+1);
            values[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, songList.get(i).getId());
        }

        // Add to db
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", id);
        ContentResolver resolver = context.getContentResolver();
        resolver.bulkInsert(uri, values);
        resolver.notifyChange(Uri.parse("content://media"), null);
    }

    private static int getCountWithPlaylistId(final Context context, final long playlistId) {

        // Get cursor
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                new String[]{BaseColumns._ID}, MediaStore.Audio.AudioColumns.IS_MUSIC + "=1"
                        + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''", null, null);

        // Get count
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                return cursor.getCount();
            }
            cursor.close();
        }

        return 0;
    }


    /*
    ALBUM UTILITIES:
     */
    public static void renameAlbum(long inputId, String name, Context context) {

        // Update all names of albums in songs
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.ALBUM, name);
        context.getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values, MediaStore.Audio.Media.ALBUM_ID + "=" + inputId, null);
    }

    public static List<Song> getSongsFromAlbum(long inputAlbumId, Context context) {

        // Get data
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATE_ADDED
        };
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
                "is_music != 0 and album_id = " + inputAlbumId, null, null);

        // Go through cursor
        List<Song> songList = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {

                // Get the data
                long id = cursor.getLong(0);
                String title = cursor.getString(1);
                String path = cursor.getString(2);
                String artist = cursor.getString(3);
                long artistId = cursor.getLong(4);
                String album = cursor.getString(5);
                long albumId = cursor.getLong(6);
                int duration = cursor.getInt(7);
                int date = cursor.getInt(8);

                // Create media player
                MediaPlayer mp = MediaPlayer.create(context, Uri.parse(path));
                if (mp != null) {

                    // Create model and add to list
                    Song song = new Song(id, title, path, artist, artistId, album, albumId, duration, date);
                    songList.add(song);
                }

            } while (cursor.moveToNext());
            cursor.close();
        }

        return songList;
    }

    public static Uri getAlbumArt(long albumId) {
        Uri uri =  ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
        if (uri != null) {
            // Log.e(TAG, "Returning " + uri.getPath() + " for " + Long.toString(albumId));
            return uri;
        } else {
            return Uri.parse("android.resource://com.lunchareas.echo/" + R.drawable.ic_album);
        }
    }

    public static void changeAlbumArt(String path, long albumId, Context context) {

        // Delete data
        Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        try {
            context.getContentResolver().delete(ContentUris.withAppendedId(artworkUri, albumId), null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Reinsert data
        if (new File(path).exists()) {
            ContentValues values = new ContentValues();
            values.put("album_id", albumId);
            values.put("_data", path);
            context.getContentResolver().insert(artworkUri, values);
        }
    }

    public static void changeAlbumArtWithSongId(String path, long songId, Context context) {

        // Find album id from song id
        long albumId = 0;
        boolean found = false;
        for (Song song: SongLoader.getSongList(context)) {
            if (song.getId() == songId) {
                albumId = song.getAlbumId();
                found = true;
            }
        }
        if (!found) {
            Log.e(TAG, "Could not find song with that ID when finding the album id.");
            return;
        }

        // Delete previous data
        Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        try {
            context.getContentResolver().delete(ContentUris.withAppendedId(artworkUri, albumId), null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Reinsert data
        if (new File(path).exists()) {
            ContentValues values = new ContentValues();
            values.put("album_id", albumId);
            values.put("_data", path);
            context.getContentResolver().insert(artworkUri, values);
        }
    }


    /*
    ARTIST UTILITIES:
     */
    public static void renameArtist(long inputId, String name, Context context) {

        // Update all names of albums in songs
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.ARTIST, name);
        context.getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values, MediaStore.Audio.Media.ARTIST_ID + "=" + inputId, null);
    }

    public static List<Song> getSongsFromArtist(long inputArtistId, Context context) {

        // Get data
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATE_ADDED
        };
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
                "is_music != 0 and artist_id = " + inputArtistId, null, null);

        // Go through cursor
        List<Song> songList = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {

                // Get the data
                long id = cursor.getLong(0);
                String title = cursor.getString(1);
                String path = cursor.getString(2);
                String artist = cursor.getString(3);
                long artistId = cursor.getLong(4);
                String album = cursor.getString(5);
                long albumId = cursor.getLong(6);
                int duration = cursor.getInt(7);
                int date = cursor.getInt(8);

                // Create media player
                MediaPlayer mp = MediaPlayer.create(context, Uri.parse(path));
                if (mp != null) {

                    // Create model and add to list
                    Song song = new Song(id, title, path, artist, artistId, album, albumId, duration, date);
                    songList.add(song);
                }

            } while (cursor.moveToNext());
            cursor.close();
        }

        return songList;
    }
}
