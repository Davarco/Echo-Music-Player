package com.lunchareas.divertio.activities;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.lunchareas.divertio.activities.MainActivity.MUSIC_DIR_NAME;


public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getName();
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 501;

    protected int id;
    protected AudioManager am;
    protected List<SongData> songInfoList;
    protected List<PlaylistData> playlistInfoList;

    protected SongUtil songUtil;
    protected PlaylistUtil playlistUtil;

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

        // External storage permissions
        if (Build.VERSION.SDK_INT < 23) {
            Log.d(TAG, "Don't need permissions.");
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Log.d(TAG, "PERMISSIONS: App needs permissions to read external storage.");
                }
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }

        // Create directory for files if it does not exist
        File musicFolder = new File(Environment.getExternalStorageDirectory() + File.separator + MUSIC_DIR_NAME);
        if (!musicFolder.exists()) {
            musicFolder.mkdir();
        }

        // Add fonts
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
        .setDefaultFontPath("fonts/Lato-Medium.ttf")
        .setFontAttrId(R.attr.fontPath)
        .build());

        // Differs due to different activities
        setContentView(id);
        super.onCreate(savedInstanceState);
        setDisplay();

        // Get context
        context = getApplicationContext();

        // Create utils
        songUtil = new SongUtil(context);
        playlistUtil = new PlaylistUtil(context);

        // Get all playlists
        PlaylistDBHandler dbPlaylist = new PlaylistDBHandler(this);
        playlistInfoList = dbPlaylist.getPlaylistDataList();

        // Get all songs
        SongDBHandler db = new SongDBHandler(this);
        songInfoList = db.getSongDataList();
    }

    protected abstract void initSongBar();

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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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
        SongDBHandler db = new SongDBHandler(context);
        this.songInfoList = db.getSongDataList();
    }

    public void updatePlaylistInfoList() {
        PlaylistDBHandler db = new PlaylistDBHandler(context);
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
