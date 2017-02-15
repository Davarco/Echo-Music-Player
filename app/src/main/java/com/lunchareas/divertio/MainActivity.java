package com.lunchareas.divertio;

import android.Manifest;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.content.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.lang.*;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 501;

    private int currentPosition;
    private ArrayList<SongData> songInfoList;
    private ListView songView;
    private boolean firstInstance;
    private boolean musicPlayerExists;

    //private boolean songBarOn;
    //private Button songBarToggle;
    private BroadcastReceiver songBroadcastReceiver;
    private SeekBar songProgressManager;
    private ImageButton songCtrlButton;
    private ImageButton songLastButton;
    private ImageButton songNextButton;

    private boolean musicBound;
    private Intent musicCreateIntent;
    private Intent musicStartIntent;
    private Intent musicPauseIntent;
    private Intent musicChangeIntent;
    private PlayMusicService musicSrv;

    private boolean drawerOpen;
    private String[] menuItemArr;
    private Button menuToggleButton;
    private RelativeLayout menuDrawerLayout;
    private DrawerLayout menuDrawer;
    private ListView menuList;

    @Override
    //@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    protected void onCreate(Bundle savedInstanceState) {

        // set the toolbar and the layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar mainBar = (Toolbar)findViewById(R.id.header_bar);
        setSupportActionBar(mainBar);

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

        // -1 because no song is playing
        final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        currentPosition = -1;

        // song list
        firstInstance = true;
        musicPlayerExists = false;
        songInfoList = new ArrayList<>();
        songView = (ListView) findViewById(R.id.song_list);
        setSongListView();

        // song bar
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
        //menuDrawer.closeDrawer(GravityCompat.START);
        drawerOpen = false;

        // create directory for files if it does not exist
        File musicFolder = new File(Environment.getExternalStorageDirectory() + File.separator + "Divertio");
        if (!musicFolder.exists()) {
            musicFolder.mkdir();
        }

        // create directory for file info if it does not exist
        File musicInfoFolder = new File(getApplicationContext().getFilesDir(), "DivertioInfoFiles");
        if (!musicInfoFolder.exists()) {
            musicInfoFolder.mkdir();
        }

        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectMenuItem(i);
            }
        });

        songView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("Detected click on song item in list view.");
                if (!musicBound) {
                    songCtrlButton.setBackgroundResource(R.drawable.pause_red);
                    String path = songInfoList.get(position).getSongPath();
                    sendMusicCreateIntent(path);
                    musicBound = true;
                } else if (position != currentPosition) {
                    songCtrlButton.setBackgroundResource(R.drawable.pause_red);
                    String wantedPath = songInfoList.get(position).getSongPath();
                    sendMusicPauseIntent();
                    sendMusicCreateIntent(wantedPath);
                }
            }
        });

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
                songCtrlButton.setBackgroundResource(R.drawable.pause_red);
            }
        });

        songBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int songPosition = intent.getIntExtra(PlayMusicService.PLAYMUSIC_POSITION, 0);
                int songDuration = intent.getIntExtra(PlayMusicService.PLAYMUSIC_DURATION, 0);

                // set location based on factor to 100000
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

    // fix crashing on destroy?
    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("Destroying activity!");
    }

    // for broadcast managing from play_red music service
    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((songBroadcastReceiver), new IntentFilter(PlayMusicService.PLAYMUSIC_RESULT));
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
        inflater.inflate(R.menu.song_overflow_menu, menu);
        return true;
    }

    // options for overflow menu
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
            case R.id.song_menu_delete: {
                System.out.println("Starting new activity - delete.");
                DialogFragment deleteDialog = new DeleteSongDialog();
                deleteDialog.show(getSupportFragmentManager(), "Delete");
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void replaceDialogWithFailure(DialogFragment d) {
        d.dismiss();
        DialogFragment uploadFailureDialog = new UploadSongFailureDialog();
        uploadFailureDialog.show(getSupportFragmentManager(), "UploadFailure");
    }

    // options for drawer menu
    private void selectMenuItem(int position) {
        System.out.println("Detected click on position " + position + ".");
        switch (position) {
            case 0: {
                System.out.println("No effect, on that activity!");
                break;
            }
            case 1: {
                System.out.println("Starting new activity - playlist.");
                Bundle b = new Bundle();
                b.putParcelableArrayList("SONGDATA", songInfoList);
                Intent i = new Intent(this, PlaylistActivity.class);
                i.putExtras(b);
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

    public void sendMusicCreateIntent(String path) {
        musicCreateIntent = new Intent(this, PlayMusicService.class);
        System.out.println("Passing string to create intent: " + path);
        musicCreateIntent.putExtra("CREATE", path);
        this.startService(musicCreateIntent);
    }

    public void sendMusicStartIntent() {
        musicStartIntent = new Intent(this, PlayMusicService.class);
        musicStartIntent.putExtra("START", 0);
        this.startService(musicStartIntent);
    }

    public void sendMusicPauseIntent() {
        musicPauseIntent = new Intent(this, PlayMusicService.class);
        musicPauseIntent.putExtra("PAUSE", 0);
        this.startService(musicPauseIntent);
    }

    public void sendMusicChangeIntent(int position) {
        musicChangeIntent = new Intent(this, PlayMusicService.class);
        musicChangeIntent.putExtra("CHANGE", position);
        this.startService(musicChangeIntent);
    }

    public void setSongListView() {
        //cleanMusicFileDir();
        getSongList();
        SongAdapter songListAdapter = new SongAdapter(this, songInfoList);
        songView.setAdapter(songListAdapter);
    }

    public ArrayList<SongData> getSongInfoList() {
        return songInfoList;
    }

    public void getSongList() {

        // clear out song info list in case new upload comes
        songInfoList.clear();

        // get the directory and print out debug info
        System.out.println("Reached getting the song list!");
        File musicInfoFolder = getApplicationContext().getDir("DivertioInfoFiles", Context.MODE_PRIVATE);
        File musicInfoLister = musicInfoFolder.getAbsoluteFile();
        for (String strFile: musicInfoLister.list()) {
            System.out.println("Name of file in music files: " + strFile);
        }

        // set the info to song data
        for (File musicInfoFile: musicInfoLister.listFiles()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(musicInfoFile));
                System.out.println("Name of file: " + musicInfoFile.getName());
                String songName = br.readLine();
                String songPath = br.readLine();
                SongData songFileData = new SongData(songName, songPath);
                songInfoList.add(songFileData);
                System.out.println("Data for \"" + songName + "\"\n" + "Song Path: " + songPath);
                br.close();
            } catch (Exception e) {
                System.out.println("File not found!");
            }
        }
    }

    public void cleanMusicFileDir() {
        File musicInfoFolder = getApplicationContext().getDir("DivertioInfoFiles", Context.MODE_PRIVATE);
        File musicInfoLister = musicInfoFolder.getAbsoluteFile();
        for (File strFile: musicInfoLister.listFiles()) {
            strFile.delete();
        }

        musicInfoFolder.delete();
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
