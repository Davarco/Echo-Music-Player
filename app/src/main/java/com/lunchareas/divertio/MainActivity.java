package com.lunchareas.divertio;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.content.*;

import java.io.File;
import java.util.*;
import java.lang.*;

public class MainActivity extends BaseActivity {

    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 501;
    public static final String MUSIC_DIR_NAME = "Divertio";

    private int currentPosition;
    private List<SongData> songInfoList;
    private ListView songView;

    public MainActivity() {
        super(R.layout.activity_main);
    }

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {

        // set the toolbar and the layout
        super.onCreate(savedInstanceState);
        Toolbar mainBar = (Toolbar)findViewById(R.id.header_bar);
        setSupportActionBar(mainBar);
        this.setTitle("Divertio");

        // external storage permissions
        if (Build.VERSION.SDK_INT < 23) {
            System.out.println("Don't need permissions.");
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    System.out.println("PERMISSIONS: App needs permissions to read external storage.");
                }
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }

        // song
        songInfoList = new ArrayList<>();
        songView = (ListView) findViewById(R.id.song_list);
        setSongListView();

        // -1 because no song is playing
        final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        currentPosition = -1;

        // create directory for files if it does not exist
        File musicFolder = new File(Environment.getExternalStorageDirectory() + File.separator + MUSIC_DIR_NAME);
        if (!musicFolder.exists()) {
            musicFolder.mkdir();
        }

        songView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                songCtrlButton.setBackgroundResource(R.drawable.pause_red);
                String wantedPath = songInfoList.get(position).getSongPath();
                sendMusicPauseIntent();
                sendMusicCreateIntent(wantedPath);
                musicBound = true;
            }
        });
    }

    @Override
    protected void setDisplay() {
        setContentView(R.layout.activity_main);
    }

    public void replaceDialogWithFailure(DialogFragment d) {
        d.dismiss();
        DialogFragment uploadFailureDialog = new UploadSongFailureDialog();
        uploadFailureDialog.show(getSupportFragmentManager(), "UploadFailure");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.song_overflow_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println("Detected that position " + item.getItemId() + " was selected.");
        switch (item.getItemId()) {
            case R.id.song_menu_upload: {
                System.out.println("Starting new activity - upload.");
                DialogFragment uploadDialog = new UploadSongDialog();
                uploadDialog.show(getSupportFragmentManager(), "Upload");
                return true;
            }
            case R.id.playlist_menu_delete: {
                System.out.println("Starting new activity - delete.");
                DialogFragment deleteDialog = new DeleteSongDialog();
                deleteDialog.show(getSupportFragmentManager(), "Delete");
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // options for drawer menu
    @Override
    protected void selectMenuItem(int position) {
        System.out.println("Detected click on position " + position + ".");
        switch (position) {
            case 0: {
                System.out.println("No effect, on that activity!");
                break;
            }
            case 1: {
                System.out.println("Starting new activity - playlist.");
                Intent i = new Intent(this, PlaylistActivity.class);
                startActivity(i);
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

    public void setSongListView() {
        //cleanMusicFileDir();
        getSongsForActivity();
        SongAdapter songListAdapter = new SongAdapter(this, songInfoList);
        songView.setAdapter(songListAdapter);
    }

    public void getSongsForActivity() {

        // get database and song list
        SongDBHandler db = new SongDBHandler(this);
        songInfoList = db.getSongDataList();
    }

    // not going to use for now, but could be useful later on
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                System.out.println("Service is running.");
                return true;
            }
        }
        System.out.println("Service is not running.");
        return false;
    }
}
