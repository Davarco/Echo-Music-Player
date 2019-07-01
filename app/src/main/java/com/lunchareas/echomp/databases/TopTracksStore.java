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
package com.lunchareas.echomp.databases;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;


public class TopTracksStore extends SQLiteOpenHelper {

    private static final String TAG = TopTracksStore.class.getName();

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "TopTracksDatabase";

    private static final String TABLE_TOP_TRACKS = "songs";
    private static final String KEY_ID = "id";
    private static final String KEY_PLAYED = "played";

    private static final int KEY_ID_IDX = 0;
    private static final int KEY_PLAYED_IDX = 1;

    public TopTracksStore(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SONG_DATABASE =
                "CREATE TABLE " + TABLE_TOP_TRACKS + "(" + KEY_ID + " INTEGER," + KEY_PLAYED + " INTEGER" + ")";
        db.execSQL(CREATE_SONG_DATABASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldDb, int newDb) {

        // Replace old table
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TOP_TRACKS);
        this.onCreate(db);
    }

    public void addSong(long id) {

        // Get table data
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Insert new data from song data
        values.put(KEY_ID, id);
        values.put(KEY_PLAYED, 0);
        db.insert(TABLE_TOP_TRACKS, null, values);
        db.close();
    }

    public void incSong(long id) {

        // Check if song exists
        TopTracksPair pair = getSong(id);
        int played;
        if (pair == null) {
            addSong(id);
            played = 0;
        } else {
            played = pair.getVal();
        }

        // Get table data
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Update data
        values.put(KEY_ID, id);
        values.put(KEY_PLAYED, played+1);
        db.update(TABLE_TOP_TRACKS, values, KEY_ID + " = ?", new String[]{ String.valueOf(id) });
        db.close();
    }

    public void decAll() {

        // Get all the songs
        List<TopTracksPair> pairList = getPairList();

        // Get table data
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Update data
        for (TopTracksPair pair: pairList) {
            values.put(KEY_ID, pair.getKey());
            values.put(KEY_PLAYED, pair.getVal()*0.1);
            db.update(TABLE_TOP_TRACKS, values, KEY_ID + " = ?", new String[]{ String.valueOf(pair.getKey()) });
        }
        db.close();
    }

    public TopTracksPair getSong(long id) {

        // Get table data
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_TOP_TRACKS, new String[] { KEY_ID, KEY_PLAYED }, KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);

        // Search through database
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            TopTracksPair pair = new TopTracksPair(id, cursor.getInt(KEY_PLAYED_IDX));
            cursor.close();
            return pair;
        }

        db.close();
        return null;
    }

    public List<TopTracksPair> getPairList() {

        // Create list and get table data
        List<TopTracksPair> pairList = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        String dbQuery = "SELECT * FROM " + TABLE_TOP_TRACKS;
        Cursor cursor = db.rawQuery(dbQuery, null);

        // Go through database and all to list
        if (cursor.moveToFirst()) {
            do {
                TopTracksPair pair = new TopTracksPair(cursor.getLong(KEY_ID_IDX), cursor.getInt(KEY_PLAYED_IDX));
                pairList.add(pair);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return pairList;
    }
}
