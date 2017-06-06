package com.lunchareas.divertio.activities;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lunchareas.divertio.fragments.CreatePlaylistNameFailureDialog;
import com.lunchareas.divertio.R;
import com.lunchareas.divertio.models.PlaylistDBHandler;
import com.lunchareas.divertio.models.PlaylistData;
import com.lunchareas.divertio.models.SongDBHandler;
import com.lunchareas.divertio.models.SongData;
import com.lunchareas.divertio.utils.PlaylistUtil;
import com.lunchareas.divertio.utils.SongUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getName();

    private int id;
    protected AudioManager am;
    protected List<SongData> songInfoList;
    protected List<PlaylistData> playlistInfoList;

    protected SongUtil songUtil;
    protected PlaylistUtil playlistUtil;

    protected boolean drawerOpen;
    protected String[] menuItemArr;
    protected Button menuToggleButton;
    protected RelativeLayout menuDrawerLayout;
    protected DrawerLayout menuDrawer;
    protected ListView menuList;

    protected Toolbar mainBar;
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

    protected Context context;

    /*
    Probably should find a better way of initializing the view.
     */
    public BaseActivity(int id) {
        this.id = id;
    }

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {

        // Differs due to different activities
        setContentView(id);
        super.onCreate(savedInstanceState);
        setDisplay();
        setBackground();

        // Get context
        context = getApplicationContext();

        // Create utils
        songUtil = new SongUtil(context);
        playlistUtil = new PlaylistUtil(context);

        // Get all playlists
        PlaylistDBHandler dbPlaylist = new PlaylistDBHandler(this);
        playlistInfoList = dbPlaylist.getPlaylistDataList();

        // Setup toolbar
        mainBar = (Toolbar) findViewById(R.id.header_bar);
        setSupportActionBar(mainBar);

        // Set new font for title
        TextView barTitle = (TextView) findViewById(R.id.bar_title);
        try {
            barTitle.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/RobotoSlab-Regular.ttf"));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Could not open the font.");
        }

        // Get song info
        SongDBHandler db = new SongDBHandler(this);
        songInfoList = db.getSongDataList();

        // Setup song bar
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        songProgressManager = (SeekBar) findViewById(R.id.progress_bar);
        songProgressManager.getProgressDrawable().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        songProgressManager.getThumb().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        songCtrlButton = (ImageButton) findViewById(R.id.play_button);
        //songBarOn = false;
        if (am.isMusicActive()) {
            musicBound = true;
            songCtrlButton.setBackgroundResource(R.drawable.pause);
        } else {
            musicBound = false;
            songCtrlButton.setBackgroundResource(R.drawable.play);
        }

        // Create the menu drawer
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

        menuToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Detected click on menu button.");
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
    }

    private void setBackground() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.activity_main).setBackground(getResources().getDrawable(R.drawable.wallpaper));
            }
        });
    }

    public void sendMusicCreateIntent(String path) {
        musicCreateIntent = new Intent(this, PlayMusicService.class);
        Log.d(TAG, "Passing string to create intent: " + path);
        musicCreateIntent.putExtra(PlayMusicService.MUSIC_CREATE, path);
        this.startService(musicCreateIntent);
    }

    public void sendMusicStartIntent() {
        musicStartIntent = new Intent(this, PlayMusicService.class);
        musicStartIntent.putExtra(PlayMusicService.MUSIC_START, 0);
        this.startService(musicStartIntent);
    }

    public void sendMusicPauseIntent() {
        musicPauseIntent = new Intent(this, PlayMusicService.class);
        musicPauseIntent.putExtra(PlayMusicService.MUSIC_PAUSE, 0);
        this.startService(musicPauseIntent);
    }

    public void sendMusicChangeIntent(int position) {
        musicChangeIntent = new Intent(this, PlayMusicService.class);
        musicChangeIntent.putExtra(PlayMusicService.MUSIC_CHANGE, position);
        this.startService(musicChangeIntent);
    }

    // For broadcast managing from play music service
    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((songBroadcastReceiver), new IntentFilter(PlayMusicService.MUSIC_RESULT));
        Log.d(TAG, "Running start!");
        menuDrawer.closeDrawers();
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(songBroadcastReceiver);
        super.onStop();
    }

    protected abstract void selectMenuItem(int position);

    protected abstract void setDisplay();

    public abstract void showChoiceMenu(View view, final int pos);

    public abstract boolean onCreateOptionsMenu(Menu menu);
    
    public abstract void setMainView();

    public List<SongData> getSongInfoList() {
        updateSongInfoList();
        return this.songInfoList;
    }

    public List<PlaylistData> getPlaylistInfoList() {
        updatePlaylistInfoList();
        return this.playlistInfoList;
    }

    public void updateSongInfoList() {
        SongDBHandler db = new SongDBHandler(getApplicationContext());
        this.songInfoList = db.getSongDataList();
    }

    public void updatePlaylistInfoList() {
        PlaylistDBHandler db = new PlaylistDBHandler(getApplicationContext());
        this.playlistInfoList = db.getPlaylistDataList();
    }

    public void createPlaylistNameFailureDialog() {
        DialogFragment dialogFragment = new CreatePlaylistNameFailureDialog();
        dialogFragment.show(getSupportFragmentManager(), "CreatePlaylistNameFailure");
    }

    public List<SongData> getSongsFromIndexes(List<Integer> songIdxList) {
        List<SongData> songList = new ArrayList<>();
        for (Integer integer: songIdxList) {
            songList.add(songInfoList.get(integer));
        }

        return songList;
    }

    public List<PlaylistData> getPlaylistsFromIndexes(List<Integer> playlistIdxList) {
        List<PlaylistData> playlistList = new ArrayList<>();
        for (Integer integer: playlistIdxList) {
            playlistList.add(playlistInfoList.get(integer));
        }

        return playlistList;
    }
}
