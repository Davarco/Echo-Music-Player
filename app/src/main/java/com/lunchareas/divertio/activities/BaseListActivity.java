package com.lunchareas.divertio.activities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.adapters.MenuDrawerAdapter;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public abstract class BaseListActivity extends BaseActivity {

    private static final String TAG = BaseListActivity.class.getName();

    protected Toolbar mainBar;
    protected boolean drawerOpen;
    protected String[] menuTitleArr;
    protected int[] menuIconArr;
    protected RelativeLayout menuDrawerLayout;
    protected DrawerLayout menuDrawer;
    protected ListView menuList;
    protected String currSong;
    protected boolean isChanging;

    protected BroadcastReceiver songBroadcastReceiver;
    protected SeekBar songProgressManager;
    protected ImageButton songCtrlButton;

    public BaseListActivity(int id) {
        super(id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init toolbar
        initToolbar();

        // Init song bar
        initSongbar();

        // Init menu
        initMenu();

        // Init list
        initList();
    }

    protected void initToolbar() {

        // Setup toolbar
        mainBar = (Toolbar) findViewById(R.id.header_bar);
        setSupportActionBar(mainBar);
    }

    protected void initSongbar() {

        // Setup song bar
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        songProgressManager = (SeekBar) findViewById(R.id.progress_bar);
        songProgressManager.getProgressDrawable().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        songProgressManager.getThumb().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        songCtrlButton = (ImageButton) findViewById(R.id.play_button);
        songCtrlButton.setBackgroundResource(R.drawable.ic_pause);
        musicBound = true;

        // Setup play button
        songCtrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log.d(TAG, "Detected click on play button.");
                if (musicBound) {
                    sendMusicPauseIntent();
                    songCtrlButton.setBackgroundResource(R.drawable.ic_play);
                } else {
                    sendMusicStartIntent();
                    songCtrlButton.setBackgroundResource(R.drawable.ic_pause);
                }
            }
        });

        // Setup progress manager
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
                isChanging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Resumes regular music from pause
                sendMusicStartIntent();
                songCtrlButton.setBackgroundResource(R.drawable.ic_pause);
                isChanging = false;
            }
        });

        // Setup time manager
        songBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // Get if music is playing
                boolean status = intent.getBooleanExtra(PlayMediaService.MUSIC_STATUS, false);
                if (musicBound != status && !isChanging) {
                    if (status) {
                        songCtrlButton.setBackgroundResource(R.drawable.ic_pause);
                    } else {
                        songCtrlButton.setBackgroundResource(R.drawable.ic_play);
                    }
                }
                musicBound = status;

                // Get current song
                currSong = intent.getStringExtra(PlayMediaService.MUSIC_CURR);

                // Set location based on position/duration
                int songPosition = intent.getIntExtra(PlayMediaService.MUSIC_POSITION, 0);
                int songDuration = intent.getIntExtra(PlayMediaService.MUSIC_DURATION, 0);
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
    }

    protected void initMenu() {

        // Create the menu drawer
        menuDrawerLayout = (RelativeLayout) findViewById(R.id.menu_drawer_layout);
        menuDrawer = (DrawerLayout) findViewById(R.id.menu_drawer);
        menuList = (ListView) findViewById(R.id.menu_drawer_list);
        menuTitleArr = new String[]{"Library", "Playlists", "Albums", "Artists", "Genres", "Now Playing"};
        menuIconArr = new int[]{R.drawable.ic_library, R.drawable.ic_menu_playlist, R.drawable.ic_album, R.drawable.ic_artist, R.drawable.ic_genre, R.drawable.ic_menu_play};
        menuList.setAdapter(new MenuDrawerAdapter(this, menuTitleArr, menuIconArr));
        menuDrawer.closeDrawers();
        drawerOpen = false;

        // Add hamburger icon to menu drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, menuDrawer, mainBar, R.string.dialog_confirm, R.string.dialog_cancel);
        menuDrawer.setDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();
        mainBar.setTitleTextColor(Color.WHITE);
        mainBar.showOverflowMenu();

        // Set listener
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectMenuItem(i);
            }
        });
    }

    protected void selectMenuItem(int position) {
        Log.d(TAG, "Detected click on position " + position + ".");
        switch (position) {
            case 0: {
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
                finish();
                break;
            }
            case 1: {
                Intent i = new Intent(this, PlaylistMenuActivity.class);
                startActivity(i);
                finish();
                break;
            }
            case 2: {
                Intent i = new Intent(this, AlbumMenuActivity.class);
                startActivity(i);
                finish();
                break;
            }
            case 3: {
                Intent i = new Intent(this, ArtistMenuActivity.class);
                startActivity(i);
                finish();
                break;
            }
            case 4: {
                Intent i = new Intent(this, GenreMenuActivity.class);
                startActivity(i);
                finish();
                break;
            }
            case 5: {
                Intent i = new Intent(this, MusicActivity.class);
                i.putExtra(MusicActivity.MUSIC_NAME, currSong);
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

    protected abstract void initList();

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((songBroadcastReceiver), new IntentFilter(PlayMediaService.MUSIC_RESULT));
        menuDrawer.closeDrawers();
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(songBroadcastReceiver);
        super.onStop();
    }
}
