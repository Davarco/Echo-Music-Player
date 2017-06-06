package com.lunchareas.divertio.activities;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.adapters.SongSelectionAdapter;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public abstract class BaseListActivity extends BaseActivity {

    private static final String TAG = BaseListActivity.class.getName();

    protected Toolbar mainBar;

    protected BroadcastReceiver songBroadcastReceiver;
    protected SeekBar songProgressManager;
    protected ImageButton songCtrlButton;

    protected boolean drawerOpen;
    protected String[] menuItemArr;
    protected RelativeLayout menuDrawerLayout;
    protected DrawerLayout menuDrawer;
    protected ListView menuList;

    public BaseListActivity(int id) {
        super(id);
    }

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup toolbar
        initToolbar();

        // Setup song bar
        initSongBar();

        // Create the menu drawer
        initMenuDrawer();

        // Add hamburger icon to menu drawer
        initIcons();

        // Setup the list view
        initList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((songBroadcastReceiver), new IntentFilter(PlayMusicService.MUSIC_RESULT));
        Log.d(TAG, "Running start!");
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(songBroadcastReceiver);
        super.onStop();
    }

    protected void initToolbar() {

        // Setup the xml and give it support
        mainBar = (Toolbar) findViewById(R.id.header_bar);
        setSupportActionBar(mainBar);
    }

    @Override
    protected void initSongBar() {

        // Setup the display of the song bar
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        songProgressManager = (SeekBar) findViewById(R.id.progress_bar);
        songProgressManager.getProgressDrawable().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        songProgressManager.getThumb().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        songCtrlButton = (ImageButton) findViewById(R.id.play_button);
        if (am.isMusicActive()) {
            musicBound = true;
            songCtrlButton.setBackgroundResource(R.drawable.pause);
        } else {
            musicBound = false;
            songCtrlButton.setBackgroundResource(R.drawable.play);
        }

        // Play button click listener
        songCtrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Detected click on play button.");
                if (musicBound) {
                    sendMusicPauseIntent();
                    songCtrlButton.setBackgroundResource(R.drawable.play);
                    musicBound = false;
                } else {
                    sendMusicStartIntent();
                    songCtrlButton.setBackgroundResource(R.drawable.pause);
                    musicBound = true;
                }
            }
        });

        // Position listener
        songBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int songPosition = intent.getIntExtra(PlayMusicService.MUSIC_POSITION, 0);
                int songDuration = intent.getIntExtra(PlayMusicService.MUSIC_DURATION, 0);

                // Set location based on position/duration
                songProgressManager.setMax(songDuration);
                songProgressManager.setProgress(songPosition);

                // Set new text in time
                String songPositionTime = String.format(
                        Locale.US, "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(songPosition),
                        TimeUnit.MILLISECONDS.toSeconds(songPosition) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songPosition))
                );

                String songDurationTime = String.format(
                        Locale.US, "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(songDuration),
                        TimeUnit.MILLISECONDS.toSeconds(songDuration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songDuration))
                );

                String totalSongTime = songPositionTime + "/" + songDurationTime;
                TextView songTimeView = (TextView) findViewById(R.id.time_info);
                songTimeView.setText(totalSongTime);
            }
        };

        // Progress change listener
        songProgressManager.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int position, boolean userPressed) {
                if (userPressed) {
                    sendMusicChangeIntent(position);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Prevents broken music during time change
                sendMusicPauseIntent();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Resumes regular music from pause
                sendMusicStartIntent();
                songCtrlButton.setBackgroundResource(R.drawable.pause);
            }
        });
    }

    protected void initMenuDrawer() {

        // Setup the display of the menu drawer
        menuDrawerLayout = (RelativeLayout) findViewById(R.id.menu_drawer_layout);
        menuDrawer = (DrawerLayout) findViewById(R.id.menu_drawer);
        menuList = (ListView) findViewById(R.id.menu_drawer_list);
        menuItemArr = new String[]{"Songs", "Playlists", "Bluetooth", "Settings"};
        menuList.setAdapter(new ArrayAdapter<>(this, R.layout.menu_drawer_list_item, menuItemArr));
        menuDrawer.closeDrawers();
        drawerOpen = false;

        // Add click listener to hamburger
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectMenuItem(i);
            }
        });
    }

    protected void initIcons() {

        // Setup icons on toolbar
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, menuDrawer, mainBar, R.string.dialog_confirm, R.string.dialog_cancel);
        menuDrawer.setDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();
        mainBar.setTitleTextColor(Color.WHITE);
        mainBar.showOverflowMenu();
    }

    protected abstract void initList();
}
