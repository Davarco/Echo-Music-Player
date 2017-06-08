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

        // Get data
        getDispData();

        // Show disp data
        showDispData();

        // Init song bar
        initSongbar();
    }

    protected abstract void initToolbar();

    protected abstract void initViews();

    protected abstract void getDispData();

    protected abstract void showDispData();

    protected abstract void initSongbar();

    protected void selectMenuItem(int position) {
        Log.d(TAG, "Detected click on position " + position + ".");
        switch (position) {
            case 0: {
                Log.d(TAG, "Starting new activity - main!");
                Intent i = new Intent(this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                break;
            }
            case 1: {
                Log.d(TAG, "Starting new activity - playlist.");
                Intent i = new Intent(this, PlaylistMenuActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                break;
            }
            /*
            case 2: {
                Log.d(TAG, "Starting new activity - bluetooth.");
                Intent i = new Intent(this, BluetoothActivity.class);
                startActivity(i);
                break;
            }
            case 3: {
                Log.d(TAG, "Starting new activity - settings.");
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
            }
            */
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((songBroadcastReceiver), new IntentFilter(PlayMusicService.MUSIC_RESULT));
        Log.d(TAG, "Running start!");
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(songBroadcastReceiver);
        super.onStop();
    }
}
