package com.lunchareas.echomp.activities;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lunchareas.echomp.R;
import com.lunchareas.echomp.services.MediaService;
import com.lunchareas.echomp.utils.Constants;
import com.lunchareas.echomp.utils.MediaControlUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public abstract class ListPlayerActivity extends BasePlayerActivity {

    private static final String TAG = ListPlayerActivity.class.getName();
    private Activity activity = this;

    protected TextView listName;
    protected ListView songListView;
    protected ImageView containerBackground;
    private ImageView listController;
    private SeekBar progressBar;
    private ImageSwitcher playButton;
    private Handler handler;

    private boolean isChanging;
    private boolean musicBound;

    public ListPlayerActivity() {
        super(R.layout.activity_list_player);
    }

    @Override
    @SuppressLint("NewApi")
    protected void initMainView() {

        // Get the views
        listName = (TextView) findViewById(R.id.name);
        songListView = (ListView) findViewById(R.id.song_list);
        containerBackground = (ImageView) findViewById(R.id.background);
        listController = (ImageView) findViewById(R.id.controller);
        progressBar = (SeekBar) findViewById(R.id.songbar_progress);
        playButton = (ImageSwitcher) findViewById(R.id.songbar_play);

        // Change progress bar style
        ListPlayerActivity.this.setTitle("");
        progressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        progressBar.getThumb().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
    }

    protected void initListener() {

        // Set button listener
        if (((AudioManager) activity.getSystemService(Context.AUDIO_SERVICE)).isMusicActive()) {
            playButton.setBackgroundResource(R.drawable.ic_pause);
        }
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicBound) {
                    MediaControlUtils.pause(activity);
                    playButton.setBackgroundResource(R.drawable.ic_play);
                } else {
                    MediaControlUtils.start(activity);
                    playButton.setBackgroundResource(R.drawable.ic_pause);
                }
            }
        });
        listController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaControlUtils.startQueueShuffled(activity, songList);
                playButton.setBackgroundResource(R.drawable.ic_pause);
                Intent intent = new Intent(activity, MusicPlayerActivity.class);
                startActivity(intent);
            }
        });
        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaControlUtils.startQueueRepeatingSpecific(activity, songList, position);
                playButton.setBackgroundResource(R.drawable.ic_pause);
                Intent intent = new Intent(activity, MusicPlayerActivity.class);
                startActivity(intent);
            }
        });

        // Receive changes in bar
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int position, boolean userPressed) {
                if (userPressed) {
                    MediaControlUtils.seek(activity, position);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isChanging = true;
                MediaControlUtils.pause(activity);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MediaControlUtils.start(activity);
                isChanging = false;
            }
        });

        // Get receiver
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                try {

                    // Get music status
                    boolean status = MediaService.getStatus();
                    if (musicBound != status && !isChanging) {
                        if (status) {
                            playButton.setBackgroundResource(R.drawable.ic_pause);
                        } else {
                            playButton.setBackgroundResource(R.drawable.ic_play);
                        }
                    }
                    musicBound = status;

                    // Update if different song
                    long tempSong = MediaService.getCurrSong();
                    if (tempSong != currSong) {
                        currSong = tempSong;
                        updateDispData();
                    }

                    // Set location based on position/duration
                    int songPosition = MediaService.getSongPos();
                    int songDuration = MediaService.getSongDur();
                    if (!isChanging && songPosition != Constants.MEDIA_ERROR && songDuration != Constants.MEDIA_ERROR) {
                        progressBar.setMax(songDuration);
                        progressBar.setProgress(songPosition);

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
                        TextView songTimeView = (TextView) findViewById(R.id.songbar_time);
                        songTimeView.setText(totalSongTime);
                    }

                } catch (Exception ignored) { ignored.printStackTrace(); }

                handler.postDelayed(this, Constants.HANDLER_DELAY);
            }
        }, Constants.HANDLER_DELAY);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onDestroy() {
        PicassoTools.clearCache(Picasso.with(this));
        System.gc();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow_menu_list_player, menu);
        return true;
    }
}
