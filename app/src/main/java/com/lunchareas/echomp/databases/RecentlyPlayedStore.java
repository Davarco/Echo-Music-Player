package com.lunchareas.echomp.databases;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class RecentlyPlayedStore extends SQLiteOpenHelper {

    private static final String TAG = TopTracksStore.class.getName();

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "RecentlyPlayedDatabase";

    private static final String TABLE_RECENTLY_PLAYED = "songs";
    private static final String KEY_ID = "id";
    private static final String KEY_LOC = "played";

    private static final int KEY_ID_IDX = 0;
    private static final int KEY_LOC_IDX = 1;

    public RecentlyPlayedStore(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SONG_DATABASE =
                "CREATE TABLE " + TABLE_RECENTLY_PLAYED + "(" + KEY_ID + " INTEGER," + KEY_LOC + " INTEGER" + ")";
        db.execSQL(CREATE_SONG_DATABASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldDb, int newDb) {

        // Replace old table
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECENTLY_PLAYED);
        this.onCreate(db);
    }

    public void insert(long id) {

        // Only run if data doesn't already exist
        if (!exists(id)) {

            // Get table data
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            // Push all other values
            String dbQuery = "SELECT * FROM " + TABLE_RECENTLY_PLAYED;
            Cursor cursor = db.rawQuery(dbQuery, null);

            // Go through database and increment all to list, except last
            if (cursor.moveToFirst()) {
                do {
                    ContentValues temp = new ContentValues();
                    long key = cursor.getLong(KEY_ID_IDX);
                    int loc = cursor.getInt(KEY_LOC_IDX);
                    if (loc < 6) {
                        temp.put(KEY_ID, cursor.getLong(KEY_ID_IDX));
                        temp.put(KEY_LOC, loc + 1);
                        db.update(TABLE_RECENTLY_PLAYED, temp, KEY_ID + " = ?", new String[]{String.valueOf(key)});
                    } else {
                        db.delete(TABLE_RECENTLY_PLAYED, KEY_ID + " = ?", new String[]{String.valueOf(key)});
                    }

                } while (cursor.moveToNext());
            }
            cursor.close();

            // Insert new data from song data
            values.put(KEY_ID, id);
            values.put(KEY_LOC, 1);
            db.insert(TABLE_RECENTLY_PLAYED, null, values);
            db.close();
        }
    }

    public List<RecentlyPlayedPair> getPairList() {

        // Create list and get table data
        List<RecentlyPlayedPair> pairList = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        String dbQuery = "SELECT * FROM " + TABLE_RECENTLY_PLAYED;
        Cursor cursor = db.rawQuery(dbQuery, null);

        // Go through database and all to list
        if (cursor.moveToFirst()) {
            do {
                RecentlyPlayedPair pair = new RecentlyPlayedPair(cursor.getLong(KEY_ID_IDX), cursor.getInt(KEY_LOC_IDX));
                pairList.add(pair);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        HashSet<RecentlyPlayedPair> hashSet = new HashSet<>(pairList);
        return new ArrayList<>(hashSet);
    }

    private boolean exists(long id) {

        // See if pair already exists
        List<RecentlyPlayedPair> pairs = getPairList();
        for (RecentlyPlayedPair pair: pairs) {
            if (id == pair.getKey()) {
                return true;
            }
        }

        return false;
    }
}
