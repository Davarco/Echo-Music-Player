package com.lunchareas.divertio.activities;


import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getName();

    private int id;
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
        super.onCreate(savedInstanceState);

        // Add fonts
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
        .setDefaultFontPath("fonts/Lato-Medium.ttf")
        .setFontAttrId(R.attr.fontPath)
        .build());

        // Differs due to different activities
        setContentView(id);

        // Get context
        context = getApplicationContext();

        // Create utils
        songUtil = new SongUtil(context);
        playlistUtil = new PlaylistUtil(context);

        // Get all playlists
        PlaylistDBHandler dbPlaylist = new PlaylistDBHandler(this);
        playlistInfoList = dbPlaylist.getPlaylistDataList();

        // Get song info
        SongDBHandler db = new SongDBHandler(this);
        songInfoList = db.getSongDataList();
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

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
