package com.lunchareas.divertio;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class PlaylistActivity extends BaseActivity {

    private int currentPosition;
    private List<PlaylistData> playlistInfoList;
    private ListView playlistView;

    public PlaylistActivity() {
        super(R.layout.activity_playlist);
    }

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {

        // set the toolbar and the layout
        super.onCreate(savedInstanceState);
        Toolbar mainBar = (Toolbar)findViewById(R.id.header_bar);
        setSupportActionBar(mainBar);
        this.setTitle("Playlists");

        // playlist
        playlistInfoList = new ArrayList<>();
        playlistView = (ListView) findViewById(R.id.playlist_list);
        setPlaylistView();

        // current position is -1 because no playlist is playing
        currentPosition = -1;

        playlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                System.out.println("Detected click in playlist item in list view.");
                if (!musicBound) {
                    songCtrlButton.setBackgroundResource(R.drawable.pause_red);
                    PlaylistData playlistData = playlistInfoList.get(position);
                    QueueController queueController = new QueueController(playlistData);
                    musicBound = true;
                } else if (position != currentPosition) {
                    songCtrlButton.setBackgroundResource(R.drawable.pause_red);
                    sendMusicPauseIntent();
                }
            }
        });
    }

    @Override
    protected void setDisplay() {
        setContentView(R.layout.activity_playlist);
    }

    // options for drawer menu
    @Override
    protected void selectMenuItem(int position) {
        System.out.println("Detected click on position " + position + ".");
        switch (position) {
            case 0: {
                System.out.println("Starting new activity - main.");
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
                break;
            }
            case 1: {
                System.out.println("No effect, on that activity.");
                break;
            }
            /*
            case 2: {
                System.out.println("Starting new activity - bluetooth.");
                Intent i = new Intent(this, BluetoothActivity.class);
                startActivity(i);
                break;
            }
            case 3: {
                System.out.println("Starting new activity - settings.");
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
            }
            */
        }
    }

    public void setPlaylistView() {
        getPlaylistsForActivity();
        PlaylistAdapter playlistAdapter = new PlaylistAdapter(this, playlistInfoList);
        playlistView.setAdapter(playlistAdapter);
    }

    public List<PlaylistData> getPlaylistInfoList() {
        return this.playlistInfoList;
    }

    public void getPlaylistsForActivity() {

        // get database and playlist
        PlaylistDBHandler db = new PlaylistDBHandler(this);
        playlistInfoList = db.getPlaylistDataList();
    }
}
