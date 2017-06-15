package com.lunchareas.divertio.activities;


import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lunchareas.divertio.R;

import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends BasePlayerActivity {

    private static final String TAG = AlbumActivity.class.getName();

    private String albumName;
    private List<String> albumSongList;
    private ListView albumListView;
    private RelativeLayout albumBackground;
    private TextView albumNameView;
    private ImageView playButton;

    public AlbumActivity() {
        super(R.layout.activity_album);
    }

    @Override
    protected void initToolbar() {

        // Get toolbar
        mainBar = (Toolbar) findViewById(R.id.main_bar);
        setSupportActionBar(mainBar);

        // Add back icon
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
    }

    @Override
    protected void initViews() {

        // Init views
        albumSongList = new ArrayList<>();
        albumListView = (ListView) findViewById(R.id.album_song_list);
        albumBackground = (RelativeLayout) findViewById(R.id.album_background);

        // Get play button
        playButton = (ImageView) findViewById(R.id.album_play_button);

        // Large name
        albumNameView = (TextView) findViewById(R.id.album_name);

        // Set play listener
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void initSongbar() {

    }

    @Override
    protected void getDispData() {

    }

    @Override
    protected void updateDispData() {

    }

    @Override
    protected void showDispData() {

    }
}
