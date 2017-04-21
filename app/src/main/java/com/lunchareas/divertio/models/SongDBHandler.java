package com.lunchareas.divertio.models;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SongDBHandler extends SQLiteOpenHelper {

    /*
    Eventually, I will probably want to add more features to the songs.
    Instructions:
    (X is the attribute name)
    - Add a KEY_X and a KEY_X_IDX constant
    - Add KEY_X to the "onCreate" method
    - Add KEY_X to the "addSongData" method
    - Add KEY_X to the "getSongData" method in the database query
    - Add KEY_X to the "updateSongData" method
    - Add KEY_X_IDX to the "getSongData" method in the songData constructor
    - Add KEY_X_IDX to the "getSongDataList" method in the songData constructor
     */

    private static final String TAG = SongDBHandler.class.getName();

    // Database info
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "SongInfoDatabase";

    // Database attributes
    private static final String TABLE_SONGS = "songs";
    private static final String KEY_NAME = "name";
    private static final String KEY_PATH = "path";
    private static final String KEY_ARTIST = "artist";

    // Numbers correspond to keys
    private static final int KEY_NAME_IDX = 0;
    private static final int KEY_PATH_IDX = 1;
    private static final int KEY_ARTIST_IDX = 2;

    public SongDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SONG_DATABASE = "CREATE TABLE " + TABLE_SONGS + "(" + KEY_NAME + " TEXT," + KEY_PATH + " TEXT," + KEY_ARTIST + " TEXT" + ")";
        db.execSQL(CREATE_SONG_DATABASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldDb, int newDb) {

        // Replace old table
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
        this.onCreate(db);
    }

    public void addSongData(SongData songData) {

        // Get table data
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Insert new data from song data
        values.put(KEY_NAME, songData.getSongName());
        values.put(KEY_PATH, songData.getSongPath());
        values.put(KEY_ARTIST, songData.getSongArtist());
        db.insert(TABLE_SONGS, null, values);
        db.close();
    }

    public SongData getSongData(String name) {

        // Get table data
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_SONGS, new String[] { KEY_NAME, KEY_PATH, KEY_ARTIST }, KEY_NAME + "=?", new String[] { String.valueOf(name) }, null, null, null, null);

        // Search through database
        if (cursor != null) {
            cursor.moveToFirst();
            SongData songData = new SongData(cursor.getString(KEY_NAME_IDX), cursor.getString(KEY_PATH_IDX), cursor.getString(KEY_ARTIST_IDX));
            db.close();
            //cursor.close();
            return songData;
        } else {
            Log.e(TAG, "Failed to create database cursor.");
        }

        Log.e(TAG, "Failed to find song data with that name in database.");
        return null;
    }

    public List<SongData> getSongDataList() {

        // Create list and get table data
        List<SongData> songDataList = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        String dbQuery = "SELECT * FROM " + TABLE_SONGS;
        Cursor cursor = db.rawQuery(dbQuery, null);

        // Go through database and all to list
        if (cursor.moveToFirst()) {
            do {
                SongData songData = new SongData(cursor.getString(KEY_NAME_IDX), cursor.getString(KEY_PATH_IDX), cursor.getString(KEY_ARTIST_IDX));
                songDataList.add(songData);
            } while (cursor.moveToNext());
        }

        //cursor.close();
        db.close();
        return songDataList;
    }

    // In order for this to be used, a new song data must be created
    public int updateSongData(SongData songData) {

        // Get table data
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Update data
        values.put(KEY_NAME, songData.getSongName());
        values.put(KEY_PATH, songData.getSongPath());
        values.put(KEY_ARTIST, songData.getSongArtist());
        return db.update(TABLE_SONGS, values, KEY_NAME + " = ?", new String[]{String.valueOf(songData.getSongName())});
    }

    // Name is a little different because it is the key
    public int updateSongData(SongData songData, String oldName) {

        // Get table data
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Update data
        values.put(KEY_NAME, songData.getSongName());
        values.put(KEY_PATH, songData.getSongPath());
        values.put(KEY_ARTIST, songData.getSongArtist());
        return db.update(TABLE_SONGS, values, KEY_NAME + " = ?", new String[]{String.valueOf(oldName)});
    }

    // Name is a little different, alt method
    public void updateSongData(SongData oldData, SongData newData) {

        deleteSongData(oldData);
        addSongData(newData);
    }

    // In order for this to be used, a new song data must be created
    public void deleteSongData(SongData songData) {

        // Get table data
        SQLiteDatabase db = this.getWritableDatabase();

        // Delete the found element
        db.delete(TABLE_SONGS, KEY_NAME + " = ?", new String[]{ songData.getSongName() });
        db.close();
    }
}
