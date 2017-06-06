package com.lunchareas.divertio.activities;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.SeekBar;

public abstract class BasePlayerActivity extends BaseActivity {

    private static final String TAG = BasePlayerActivity.class.getName();

    protected BroadcastReceiver songBroadcastReceiver;
    protected SeekBar songProgressManager;
    protected ImageButton songCtrlButton;

    public BasePlayerActivity(int id) {
        super(id);
    }

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the song bar
        initSongBar();
    }

    protected abstract void initSongBar();
}
