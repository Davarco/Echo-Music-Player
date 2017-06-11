package com.lunchareas.divertio.activities;


import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.lunchareas.divertio.R;

public abstract class BasePlayerActivity extends BaseActivity {

    private final static String TAG = BasePlayerActivity.class.getName();

    protected BroadcastReceiver songBroadcastReceiver;
    protected SeekBar songProgressManager;
    protected ImageView songCtrlButton;
    protected Toolbar mainBar;
    protected ListView menuList;
    protected String[] menuItemArr;
    protected DrawerLayout menuDrawer;

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

        // Get data
        getDispData();

        // Show disp data
        showDispData();
    }

    protected abstract void initToolbar();

    protected abstract void initViews();

    protected abstract void initSongbar();

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
        LocalBroadcastManager.getInstance(this).registerReceiver((songBroadcastReceiver), new IntentFilter(PlayMusicService.MUSIC_RESULT));
        //Log.d(TAG, "Running start!");
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(songBroadcastReceiver);
        super.onStop();
    }
}
