package com.lunchareas.divertio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.media.AudioManager;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PlaylistActivity extends AppCompatActivity {

    private ArrayList<SongData> songInfoList;

    private AudioManager am;
    private BroadcastReceiver songBroadcastReceiver;
    private SeekBar songProgressManager;
    private ImageButton songCtrlButton;

    private boolean musicBound;
    private Intent musicCreateIntent;
    private Intent musicStartIntent;
    private Intent musicPauseIntent;
    private Intent musicChangeIntent;

    private boolean drawerOpen;
    private String[] menuItemArr;
    private Button menuToggleButton;
    private RelativeLayout menuDrawerLayout;
    private DrawerLayout menuDrawer;
    private ListView menuList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        Toolbar mainBar = (Toolbar)findViewById(R.id.header_bar);
        setSupportActionBar(mainBar);
        this.setTitle("Playlists");

        // get song info
        SongDBHandler db = new SongDBHandler(this);
        songInfoList = (ArrayList) db.getSongDataList();
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

        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectMenuItem(i);
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
            }
        });

        songBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int songPosition = intent.getIntExtra(PlayMusicService.PLAYMUSIC_POSITION, 0);
                int songDuration = intent.getIntExtra(PlayMusicService.PLAYMUSIC_DURATION, 0);

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

    // for broadcast managing from play_red music service
    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((songBroadcastReceiver), new IntentFilter(PlayMusicService.PLAYMUSIC_RESULT));
        System.out.println("Running playlist start!");
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

    // options for overflow menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println("Detected that position " + item.getItemId() + " was selected.");
        switch (item.getItemId()) {
            case R.id.playlist_menu_create: {
                System.out.println("Starting new activity - create.");
                DialogFragment createPlaylistDialog = new CreatePlaylistDialog();
                createPlaylistDialog.show(getSupportFragmentManager(), "Upload");
                return true;
            }
            case R.id.playlist_menu_delete: {
                System.out.println("Starting new activity - delete.");
                DialogFragment deletePlaylistDialog = new DeleteSongDialog();
                deletePlaylistDialog.show(getSupportFragmentManager(), "Delete");
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // options for drawer menu
    private void selectMenuItem(int position) {
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

    public ArrayList<SongData> getSongInfoList() {
        return this.songInfoList;
    }
}
