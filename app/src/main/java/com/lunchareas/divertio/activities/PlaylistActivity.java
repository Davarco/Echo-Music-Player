package com.lunchareas.divertio.activities;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.adapters.SongFixedAdapter;
import com.lunchareas.divertio.models.PlaylistDBHandler;
import com.lunchareas.divertio.models.PlaylistData;
import com.lunchareas.divertio.models.SongData;
import com.lunchareas.divertio.utils.PlaylistQueueUtil;
//https://www.youtube.com/watch?v=1UlRIbpYTwk
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PlaylistActivity extends BasePlayerActivity {

    private static final String TAG = PlaylistActivity.class.getName();

    private List<SongData> songInfoList;
    private ListView playlistView;
    private ImageView playButton;
    private RelativeLayout playlistBackground;
    private PlaylistData playlistData;
    private TextView playlistViewName;
    private int position;

    public PlaylistActivity() {
        super(R.layout.activity_playlist_manager);
    }

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get play button
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Playing playlist.");
                songCtrlButton.setImageResource(R.drawable.ic_pause);
                sendMusicPauseIntent();
                PlaylistQueueUtil queueUtil = new PlaylistQueueUtil(playlistData, PlaylistActivity.this);
                queueUtil.startQueue();
                musicBound = true;
            }
        });

        // Just for feeling
        SongFixedAdapter songListAdapter = new SongFixedAdapter(this, songInfoList);
        playlistView.setAdapter(songListAdapter);
        playlistView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Detected LONG click on playlist song.");
                //showChoiceMenu(view, position);
                return true;
            }
        });

        // Set opaque background
        playlistBackground.getBackground().setAlpha(150);

        // Add playlist name
        playlistViewName.setText(playlistData.getPlaylistName());
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
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        songProgressManager = (SeekBar) findViewById(R.id.progress_bar);
        songProgressManager.getProgressDrawable().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        songProgressManager.getThumb().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        songCtrlButton = (ImageView) findViewById(R.id.play_button);
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
                songCtrlButton.setBackgroundResource(R.drawable.ic_pause);
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

        // Init views
        songInfoList = new ArrayList<>();
        playlistView = (ListView) findViewById(R.id.song_list);
        playlistBackground = (RelativeLayout) findViewById(R.id.playlist_background);

        // Large red button
        playButton = (ImageView) findViewById(R.id.playlist_play_button);

        // Large name
        playlistViewName = (TextView) findViewById(R.id.playlist_name);
    }

    @Override
    protected void getDispData() {

        // Get playlist data
        if (getIntent() == null) {
            Log.e(TAG, "Cannot find intent?");
        }
        if (getIntent().getExtras() == null) {
            Log.e(TAG, "Extras were not passed to playlist manager.");
        }
        String playlistName = getIntent().getStringExtra(PlaylistMenuActivity.PLAYLIST_NAME);
        PlaylistDBHandler db = new PlaylistDBHandler(this);
        playlistData = db.getPlaylistData(playlistName);
        songInfoList = playlistData.getSongList();
        position = playlistInfoList.indexOf(playlistData);
    }

    @Override
    protected void updateDispData() {

        // Get the new playlist data
        playlistData = getPlaylistInfoList().get(position);
    }

    @Override
    protected void showDispData() {
        Log.d(TAG, "Resetting main view for playlist controller activity.");

        // Set center icon image
        if (playlistBackground != null) {
            if (playlistData.getPlaylistIcon() == null) {
                Log.d(TAG, "No default playlist icon found.");

                // Try with song icon
                List<SongData> songList = playlistData.getSongList();
                Collections.shuffle(songList);
                boolean found = false;
                for (SongData songData : songList) {
                    if (songData.getSongCover() != null) {
                        playlistBackground.setBackground(songData.getSongCover());
                        found = true;
                    }
                }

                // Songs had no icon too
                if (!found) {
                    Drawable drawable = getResources().getDrawable(R.drawable.ic_media_icon);
                    playlistBackground.setBackground(drawable);
                }

            } else {
                playlistBackground.setBackground(playlistData.getPlaylistIcon());
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                Intent i = new Intent(this, PlaylistMenuActivity.class);
                startActivity(i);
                finish();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
