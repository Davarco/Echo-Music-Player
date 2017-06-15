package com.lunchareas.divertio.activities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lunchareas.divertio.R;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public abstract class BasePlayerActivity extends BaseActivity {

    private final static String TAG = BasePlayerActivity.class.getName();

    protected BroadcastReceiver songBroadcastReceiver;
    protected SeekBar songProgressManager;
    protected ImageView songCtrlButton;
    protected Toolbar mainBar;
    protected boolean isChanging;

    public BasePlayerActivity(int id) {
        super(id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init the toolbar
        initToolbar();

        // Init views
        initViews();

        // Init song bar
        initSongbar();

        // Init listener
        initListener();

        // Get data
        getDispData();

        // Show disp data
        showDispData();
    }

    protected abstract void initToolbar();

    protected abstract void initViews();

    protected abstract void initSongbar();

    protected void initListener() {

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
                isChanging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Resumes regular music from pause
                sendMusicStartIntent();
                songCtrlButton.setBackgroundResource(R.drawable.ic_pause);
                isChanging = false;
            }
        });

        // Setup time manager
        songBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // Get if music is playing
                boolean status = intent.getBooleanExtra(PlayMediaService.MUSIC_STATUS, false);
                if (musicBound != status && !isChanging) {
                    if (status) {
                        songCtrlButton.setBackgroundResource(R.drawable.ic_pause);
                    } else {
                        songCtrlButton.setBackgroundResource(R.drawable.ic_play);
                    }
                }
                musicBound = status;

                // Set location based on position/duration
                int songPosition = intent.getIntExtra(PlayMediaService.MUSIC_POSITION, 0);
                int songDuration = intent.getIntExtra(PlayMediaService.MUSIC_DURATION, 0);
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

    protected abstract void getDispData();

    protected abstract void updateDispData();

    protected abstract void showDispData();

    @Override
    public void setMainView() {
        updateDispData();
        showDispData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((songBroadcastReceiver), new IntentFilter(PlayMediaService.MUSIC_RESULT));
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(songBroadcastReceiver);
        super.onStop();
    }
}
