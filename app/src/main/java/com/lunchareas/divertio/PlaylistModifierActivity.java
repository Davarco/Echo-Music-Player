package com.lunchareas.divertio;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class PlaylistModifierActivity extends BaseActivity {

    private static final String TAG = PlaylistModifierActivity.class.getName();

    public PlaylistModifierActivity() {
        super(R.layout.activity_playlist_modifier);
    }

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setDisplay() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.playlist_modifier_overflow_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "Detected that position " + item.getItemId() + " was selected.");
        switch (item.getItemId()) {
            case R.id.playlist_rename: {
                Log.i(TAG, "Starting new dialog - rename.");
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void selectMenuItem(int position) {

    }
}
