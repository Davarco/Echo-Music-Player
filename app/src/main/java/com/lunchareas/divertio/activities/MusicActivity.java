package com.lunchareas.divertio.activities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.fragments.ChangeSongArtistDialog;
import com.lunchareas.divertio.fragments.ChangeSongTitleDialog;
import com.lunchareas.divertio.models.SongDBHandler;
import com.lunchareas.divertio.models.SongData;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MusicActivity extends BasePlayerActivity {

    private static final String TAG = MusicActivity.class.getName();

    public static final String MUSIC_NAME = "name";

    private SongData songData;
    private TextView songName;
    private TextView artistName;
    private ImageView songCover;
    private int position;

    public MusicActivity() {
        super(R.layout.activity_music);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    protected void initSongbar() {

        // Setup song bar
        if (am.isMusicActive()) {
            musicBound = true;
            songCtrlButton.setImageResource(R.drawable.ic_pause);
        } else {
            musicBound = false;
            songCtrlButton.setImageResource(R.drawable.ic_play);
        }

        // Setup play button
        songCtrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Detected click on play button.");
                if (musicBound) {
                    sendMusicPauseIntent();
                    songCtrlButton.setImageResource(R.drawable.ic_play);
                    musicBound = false;
                } else {
                    sendMusicStartIntent();
                    songCtrlButton.setImageResource(R.drawable.ic_pause);
                    musicBound = true;
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
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Resumes regular music from pause
                sendMusicStartIntent();
                songCtrlButton.setImageResource(R.drawable.ic_pause);
            }
        });

        // Setup time manager
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

    @Override
    protected void initViews() {

        // Song bar
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        songProgressManager = (SeekBar) findViewById(R.id.progress_bar);
        songProgressManager.getProgressDrawable().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        songProgressManager.getThumb().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        songCtrlButton = (ImageView) findViewById(R.id.play_button);

        // Names
        songName = (TextView) findViewById(R.id.song_name);
        artistName = (TextView) findViewById(R.id.song_composer);

        // Cover
        songCover = (ImageView) findViewById(R.id.song_cover);
    }

    @Override
    protected void getDispData() {

        // Get song name
        if (getIntent() == null) {
            Log.e(TAG, "Cannot find intent?");
        }
        if (getIntent().getExtras() == null) {
            Log.e(TAG, "Extras were not passed to playlist manager.");
        }
        String playlistName = getIntent().getStringExtra(MUSIC_NAME);
        SongDBHandler db = new SongDBHandler(this);
        songData = db.getSongData(playlistName);
        position = songInfoList.indexOf(songData);
    }

    @Override
    protected void updateDispData() {

        // Get the new data
        songData = getSongInfoList().get(position);
    }

    @Override
    protected void showDispData() {

        // Change the names
        songName.setText(songData.getSongName());
        artistName.setText(songData.getSongArtist());

        // Change the picture
        songCover.setImageDrawable(songData.getSongCover());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.music_overflow_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
                finish();
                return true;
            }
            case R.id.song_rename_title: {
                // Create popup for new title
                DialogFragment changeSongTitleDialog = new ChangeSongTitleDialog();
                Bundle bundle = new Bundle();
                bundle.putString(ChangeSongTitleDialog.MUSIC_POS, songData.getSongName());
                changeSongTitleDialog.setArguments(bundle);
                changeSongTitleDialog.show(getSupportFragmentManager(), "ChangeTitle");
            }
            case R.id.song_change_artist: {
                // Create popup for new artist
                DialogFragment dialogFragment = new ChangeSongArtistDialog();

            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
