package com.lunchareas.divertio;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public abstract class BaseActivity extends AppCompatActivity implements MusicController {

    private int id;
    protected AudioManager am;
    protected List<SongData> songInfoList;

    protected boolean drawerOpen;
    protected String[] menuItemArr;
    protected Button menuToggleButton;
    protected RelativeLayout menuDrawerLayout;
    protected DrawerLayout menuDrawer;
    protected ListView menuList;

    protected BroadcastReceiver songBroadcastReceiver;
    protected SeekBar songProgressManager;
    protected ImageButton songCtrlButton;
    protected ImageButton songLastButton;
    protected ImageButton songNextButton;

    protected Intent musicCreateIntent;
    protected Intent musicStartIntent;
    protected Intent musicPauseIntent;
    protected Intent musicChangeIntent;

    protected boolean musicBound;
    protected PlayMusicService musicSrv;

    /*
    Probably should find a better way of initializing the view.
     */
    public BaseActivity(int id) {
        this.id = id;
    }

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {

        // differs due to different activities
        setContentView(id);
        super.onCreate(savedInstanceState);
        setDisplay();

        // get song info
        SongDBHandler db = new SongDBHandler(this);
        songInfoList = db.getSongDataList();
        System.out.println(songInfoList);

        // song bar
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        songProgressManager = (SeekBar) findViewById(R.id.progress_bar);
        songProgressManager.getProgressDrawable().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        songProgressManager.getThumb().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        songCtrlButton = (ImageButton) findViewById(R.id.play_button);
        //songBarOn = false;
        if (am.isMusicActive()) {
            musicBound = true;
            songCtrlButton.setBackgroundResource(R.drawable.pause_red);
        } else {
            musicBound = false;
            songCtrlButton.setBackgroundResource(R.drawable.play_red);
        }

        // menu drawer
        menuDrawerLayout = (RelativeLayout) findViewById(R.id.menu_drawer_layout);
        menuDrawer = (DrawerLayout) findViewById(R.id.menu_drawer);
        menuToggleButton = (Button) findViewById(R.id.menu_toggle);
        menuList = (ListView) findViewById(R.id.menu_drawer_list);
        menuItemArr = new String[]{"Songs", "Playlists", "Bluetooth", "Settings"};
        menuList.setAdapter(new ArrayAdapter<>(this, R.layout.menu_drawer_list_item, menuItemArr));
        menuDrawer.closeDrawers();
        drawerOpen = false;

        songCtrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Detected click on play_red button.");
                if (musicBound) {
                    sendMusicPauseIntent();
                    songCtrlButton.setBackgroundResource(R.drawable.play_red);
                    musicBound = false;
                } else {
                    sendMusicStartIntent();
                    songCtrlButton.setBackgroundResource(R.drawable.pause_red);
                    musicBound = true;
                }
            }
        });

        menuToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Detected click on menu button.");
                if (drawerOpen) {
                    //menuDrawerLayout.setVisibility(View.GONE);
                    menuDrawer.closeDrawer(GravityCompat.START);
                    drawerOpen = false;
                } else {
                    //menuDrawerLayout.setVisibility(View.VISIBLE);
                    menuDrawer.openDrawer(GravityCompat.START);
                    drawerOpen = true;
                }
            }
        });

        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectMenuItem(i);
            }
        });

        songProgressManager.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int position, boolean userPressed) {
                if (userPressed) {
                    sendMusicChangeIntent(position);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // prevents broken music during time change
                sendMusicPauseIntent();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // resumes regular music from pause_red
                sendMusicStartIntent();
            }
        });

        songBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int songPosition = intent.getIntExtra(PlayMusicService.MUSIC_POSITION, 0);
                int songDuration = intent.getIntExtra(PlayMusicService.MUSIC_DURATION, 0);

                // set location based on position/duration
                songProgressManager.setMax(songDuration);
                songProgressManager.setProgress(songPosition);

                // set new text in time
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

    @Override
    public void sendPlaylistCreateIntent(List<SongData> songList) {
        // useless here
    }

    @Override
    public void sendMusicCreateIntent(String path) {
        musicCreateIntent = new Intent(this, PlayMusicService.class);
        System.out.println("Passing string to create intent: " + path);
        musicCreateIntent.putExtra(PlayMusicService.MUSIC_CREATE, path);
        this.startService(musicCreateIntent);
    }

    @Override
    public void sendMusicStartIntent() {
        musicStartIntent = new Intent(this, PlayMusicService.class);
        musicStartIntent.putExtra(PlayMusicService.MUSIC_START, 0);
        this.startService(musicStartIntent);
    }

    @Override
    public void sendMusicPauseIntent() {
        musicPauseIntent = new Intent(this, PlayMusicService.class);
        musicPauseIntent.putExtra(PlayMusicService.MUSIC_PAUSE, 0);
        this.startService(musicPauseIntent);
    }

    @Override
    public void sendMusicChangeIntent(int position) {
        musicChangeIntent = new Intent(this, PlayMusicService.class);
        musicChangeIntent.putExtra(PlayMusicService.MUSIC_CHANGE, position);
        this.startService(musicChangeIntent);
    }

    protected abstract void setDisplay();

    // for broadcast managing from play music service
    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((songBroadcastReceiver), new IntentFilter(PlayMusicService.MUSIC_RESULT));
        System.out.println("Running start!");
        menuDrawer.closeDrawers();
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(songBroadcastReceiver);
        super.onStop();
    }

    // overflow menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.playlist_overflow_menu, menu);
        return true;
    }

    protected abstract void selectMenuItem(int position);

    public List<SongData> getSongInfoList() {
        return this.songInfoList;
    }
}
