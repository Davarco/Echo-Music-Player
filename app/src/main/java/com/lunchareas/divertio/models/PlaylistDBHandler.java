package com.lunchareas.divertio.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PlaylistDBHandler extends SQLiteOpenHelper {

    private static final String TAG = PlaylistDBHandler.class.getName();

    // Database info
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "PlaylistInfoDatabase";

    // Database attributes
    private static final String TABLE_PLAYLISTS = "playlists";
    private static final String KEY_NAME = "name";
    private static final String KEY_LIST = "list";

    // Numbers correspond to keys
    private static final int KEY_NAME_IDX = 0;
    private static final int KEY_LIST_IDX = 1;

    // Needs context for song database handler
    private Context dbContext;

    public PlaylistDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.dbContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SONG_DATABASE = "CREATE TABLE " + TABLE_PLAYLISTS + "(" + KEY_NAME + " TEXT," + KEY_LIST + " TEXT" + ")";
        db.execSQL(CREATE_SONG_DATABASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldDb, int newDb) {

        // Replace old table
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLISTS);
        this.onCreate(db);
    }

    private String playlistToString(PlaylistData playlistData) {

        // Get usable data from list
        List<String> songNameList = new ArrayList<>();
        for (SongData songData: playlistData.getSongList()) {
            songNameList.add(songData.getSongName());
        }

        Gson gson = new Gson();
        String songListString = gson.toJson(songNameList);
        Log.d(TAG, "String: " + songListString);

        return songListString;
    }

    private List<SongData> stringToSongData(String songListString) {

        // Get usable data from string
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        ArrayList<String> finalOutputString = gson.fromJson(songListString, type);

        // Search for songs with the name
        SongDBHandler db = new SongDBHandler(dbContext);
        List<SongData> songDataList = new ArrayList<>();
        for (String songName: finalOutputString) {
            //Log.d(TAG, "Adding song: " + songName);
            songDataList.add(db.getSongData(songName));
        }

        return songDataList;
    }

    public void addPlaylistData(PlaylistData playlistData) {

        // Get table data
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Insert new data from song data
        values.put(KEY_NAME, playlistData.getPlaylistName());
        values.put(KEY_LIST, playlistToString(playlistData));

        db.insert(TABLE_PLAYLISTS, null, values);
        db.close();
    }

    public PlaylistData getPlaylistData(String name) {

        // Get table data
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_PLAYLISTS, new String[] { KEY_NAME, KEY_LIST }, KEY_NAME + "=?", new String[] { String.valueOf(name) }, null, null, null, null);

        // Search through database
        if (cursor != null) {
            cursor.moveToFirst();
            PlaylistData playlistData = new PlaylistData(cursor.getString(KEY_NAME_IDX), stringToSongData(cursor.getString(KEY_LIST_IDX)));
            db.close();
            return playlistData;
        } else {
            Log.d(TAG, "Failed to create database cursor.");
        }

        Log.d(TAG, "Failed to find song data with that name in database.");
        return null;
    }

    public List<PlaylistData> getPlaylistDataList() {

        // Create list and get table data
        List<PlaylistData> playlistDataList = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        String dbQuery = "SELECT * FROM " + TABLE_PLAYLISTS;
        Cursor cursor = db.rawQuery(dbQuery, null);

        // Go through database and all to list
        if (cursor != null && cursor.moveToFirst()) {
            do {
                PlaylistData playlistData = new PlaylistData(cursor.getString(KEY_NAME_IDX), stringToSongData(cursor.getString(KEY_LIST_IDX)));
                playlistDataList.add(playlistData);
            } while (cursor.moveToNext());
        }

        db.close();
        return playlistDataList;
    }

    public int updatePlaylistData(PlaylistData playlistData) {

        // Get table data
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Update data
        values.put(KEY_NAME, playlistData.getPlaylistName());
        values.put(KEY_LIST, playlistToString(playlistData));
        return db.update(TABLE_PLAYLISTS, values, KEY_NAME + " = ?", new String[]{String.valueOf(playlistData.getPlaylistName())});
    }

    // Name is a little different because it is the key
    public int updatePlaylistData(PlaylistData playlistData, String oldName) {

        // Get table data
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Update data
        values.put(KEY_NAME, playlistData.getPlaylistName());
        values.put(KEY_LIST, playlistToString(playlistData));
        return db.update(TABLE_PLAYLISTS, values, KEY_NAME + " = ?", new String[]{String.valueOf(oldName)});
    }

    public void deletePlaylistData(PlaylistData playlistData) {

        // Get table data
        SQLiteDatabase db = this.getWritableDatabase();

        // Delete the found element
        db.delete(TABLE_PLAYLISTS, KEY_NAME + " = ?", new String[]{ playlistData.getPlaylistName() });
        db.close();
    }
}
