package com.lunchareas.divertio.activities;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.adapters.SongAdapter;
import com.lunchareas.divertio.models.PlaylistDBHandler;
import com.lunchareas.divertio.models.PlaylistData;
import com.lunchareas.divertio.models.SongData;
import com.lunchareas.divertio.utils.PlaylistController;
//https://www.youtube.com/watch?v=1UlRIbpYTwk
import java.util.ArrayList;
import java.util.List;

public class PlaylistManagerActivity extends BaseActivity {

    private static final String TAG = PlaylistManagerActivity.class.getName();

    private List<SongData> songInfoList;
    private ListView playlistView;
    private PlaylistData playlistData;

    public PlaylistManagerActivity() {
        super(R.layout.activity_playlist_manager);
    }

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get playlist data
        if (getIntent() == null) {
            Log.e(TAG, "Cannot find intent?");
        }
        if (getIntent().getExtras() == null) {
            Log.e(TAG, "Extras were not passed to playlist manager.");
        }
        String playlistName = getIntent().getStringExtra(PlaylistActivity.PLAYLIST_NAME);
        PlaylistDBHandler db = new PlaylistDBHandler(this);
        playlistData = db.getPlaylistData(playlistName);

        // change title
        TextView titleText = (TextView) findViewById(R.id.bar_title);
        titleText.setText(playlistData.getPlaylistName());

        // songs in playlist
        songInfoList = new ArrayList<>();
        playlistView = (ListView) findViewById(R.id.song_list);
        setSongListView();

        playlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Log.d(TAG, "Detected click in playlist item in list view.");
                Log.d(TAG, "Song: " + playlistData.getSongList().get(position).getSongName());
                songCtrlButton.setBackgroundResource(R.drawable.pause_red);
                sendMusicPauseIntent();
                PlaylistController queueController = new PlaylistController(position, playlistData, PlaylistManagerActivity.this);
                queueController.startQueue();
                musicBound = true;
            }
        });
    }

    @Override
    protected void setDisplay() {
        setContentView(R.layout.activity_playlist_manager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.playlist_manager_overflow_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "Detected that position " + item.getItemId() + " was selected.");
        switch (item.getItemId()) {
            /*
            case R.id.playlist_rename: {
                Log.d(TAG, "Starting new dialog - rename.");
                return true;
            } */
            case R.id.playlist_add: {
                Log.d(TAG, "Starting new dialog - add.");
                return true;
            }
            case R.id.playlist_delete: {
                Log.d(TAG, "Starting new dialog - delete.");
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // options for drawer menu
    @Override
    protected void selectMenuItem(int position) {
        Log.d(TAG, "Detected click on position " + position + ".");
        switch (position) {
            case 0: {
                Log.d(TAG, "Starting new activity - main!");
                Intent i = new Intent(this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                break;
            }
            case 1: {
                Log.d(TAG, "Starting new activity - playlist.");
                Intent i = new Intent(this, PlaylistActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                break;
            }
            /*
            case 2: {
                Log.d(TAG, "Starting new activity - bluetooth.");
                Intent i = new Intent(this, BluetoothActivity.class);
                startActivity(i);
                break;
            }
            case 3: {
                Log.d(TAG, "Starting new activity - settings.");
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
            }
            */
        }
    }

    public void setSongListView() {
        //cleanMusicFileDir();
        getSongsForActivity();
        SongAdapter songListAdapter = new SongAdapter(this, songInfoList);
        playlistView.setAdapter(songListAdapter);
    }

    public void getSongsForActivity() {

        // get database and song list
        songInfoList = playlistData.getSongList();
    }
}
