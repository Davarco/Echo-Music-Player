package com.lunchareas.divertio.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.content.*;

import com.lunchareas.divertio.adapters.SongSelectionAdapter;
import com.lunchareas.divertio.fragments.AddToPlaylistDialog;
import com.lunchareas.divertio.fragments.ChangeSongArtistDialog;
import com.lunchareas.divertio.fragments.ChangeSongTitleDialog;
import com.lunchareas.divertio.fragments.DeleteSongDialog;
import com.lunchareas.divertio.R;
import com.lunchareas.divertio.adapters.SongAdapter;
import com.lunchareas.divertio.fragments.DownloadNameFailureDialog;
import com.lunchareas.divertio.models.SongDBHandler;
import com.lunchareas.divertio.models.SongData;
import com.lunchareas.divertio.fragments.DownloadSongDialog;
import com.lunchareas.divertio.fragments.DownloadConnectionFailureDialog;
import com.lunchareas.divertio.utils.SongUtil;

import java.io.File;
import java.util.*;
import java.lang.*;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getName();

    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 501;
    public static final String MUSIC_DIR_NAME = "Divertio";

    private int currentPosition;
    private List<SongData> songInfoList;
    private ListView songView;
    private SongSelectionAdapter selectionAdapter;

    public MainActivity() {
        super(R.layout.activity_main);
    }

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // External storage permissions
        if (Build.VERSION.SDK_INT < 23) {
            Log.d(TAG, "Don't need permissions.");
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Log.d(TAG, "PERMISSIONS: App needs permissions to read external storage.");
                }
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }

        // Create the selection adapter
        if (songInfoList == null) {
            Log.d(TAG, "No song list found yet.");
        }
        selectionAdapter = new SongSelectionAdapter(context, R.layout.song_layout, songInfoList);

        // Get song info and set the listview
        songInfoList = new ArrayList<>();
        songView = (ListView) findViewById(R.id.song_list);
        setMainView();

        // -1 because no song is playing
        final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        currentPosition = -1;

        // Create directory for files if it does not exist
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

        songView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "Detected LONG click on song.");
                // Change to selected mode if needed
                setSelectedMode();
                return true;
            }
        });
    }

    private void setSelectedMode() {

        // Set new adapter
        songView.setAdapter(selectionAdapter);

        // Set new mode and add listener
        songView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        songView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
    }

    @SuppressLint("NewApi")
    public void showSongChoiceMenu(View view, final int pos) {
        final PopupMenu popupMenu = new PopupMenu(context, view, Gravity.END);
        final SongData selectedSong = songInfoList.get(pos);

        // Handle individual clicks
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.song_rename_title: {
                        Log.d(TAG, "Renaming song title!");

                        // Create popup for new title
                        DialogFragment changeSongTitleDialog = new ChangeSongTitleDialog();
                        Bundle bundle = new Bundle();
                        bundle.putInt(ChangeSongTitleDialog.MUSIC_POS, pos);
                        changeSongTitleDialog.setArguments(bundle);
                        changeSongTitleDialog.show(getSupportFragmentManager(), "ChangeTitle");

                        return true;
                    }
                    case R.id.song_delete_title: {
                        Log.d(TAG, "Deleting song!");

                        // Remove song from list and re-update view
                        SongUtil songController = new SongUtil(context);
                        songController.deleteSong(selectedSong);
                        setMainView();

                        return true;
                    }
                    case R.id.song_change_artist: {
                        Log.d(TAG, "Changing song artist!");

                        // Create popup for new artist
                        DialogFragment changeSongArtistDialog = new ChangeSongArtistDialog();
                        Bundle bundle = new Bundle();
                        bundle.putInt(ChangeSongArtistDialog.MUSIC_POS, pos);
                        changeSongArtistDialog.setArguments(bundle);
                        changeSongArtistDialog.show(getSupportFragmentManager(), "ChangeArtist");

                        return true;
                    }
                    case R.id.song_to_playlist: {
                        Log.d(TAG, "Adding song to playlist!");

                        // Create popup to add to playlist
                        DialogFragment addToPlaylistDialog = new AddToPlaylistDialog();
                        Bundle bundle = new Bundle();
                        bundle.putInt(AddToPlaylistDialog.MUSIC_POS, pos);
                        addToPlaylistDialog.setArguments(bundle);
                        addToPlaylistDialog.show(getSupportFragmentManager(), "AddSongToPlaylist");

                        return true;
                    }
                    default: {
                        return false;
                    }
                }
            }
        });

        // Create menu and show
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.song_choice_menu, popupMenu.getMenu());
        popupMenu.show();
    }

    @Override
    protected void setDisplay() {
        setContentView(R.layout.activity_main);
    }

    public void createDownloadFailureDialog(DialogFragment d) {
        d.dismiss();
        DialogFragment uploadFailureDialog = new DownloadConnectionFailureDialog();
        uploadFailureDialog.show(getSupportFragmentManager(), "UploadFailure");
    }

    public void createNameFailureDialog(DialogFragment d) {
        d.dismiss();
        DialogFragment nameFailureDialog = new DownloadNameFailureDialog();
        nameFailureDialog.show(getSupportFragmentManager(), "NameFailure");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.song_overflow_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "Detected that position " + item.getItemId() + " was selected.");
        switch (item.getItemId()) {
            case R.id.song_menu_upload: {
                Log.d(TAG, "Starting new dialog - download.");
                DialogFragment uploadDialog = new DownloadSongDialog();
                uploadDialog.show(getSupportFragmentManager(), "Upload");
                return true;
            }
            case R.id.song_menu_delete: {
                Log.d(TAG, "Starting new dialog - delete.");
                DialogFragment deleteDialog = new DeleteSongDialog();
                deleteDialog.show(getSupportFragmentManager(), "Delete");
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Options for drawer menu
    @Override
    protected void selectMenuItem(int position) {
        Log.d(TAG, "Detected click on position " + position + ".");
        switch (position) {
            case 0: {
                Log.d(TAG, "No effect, on that activity!");
                break;
            }
            case 1: {
                Log.d(TAG, "Starting new activity - playlist.");
                Intent i = new Intent(this, PlaylistActivity.class);
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
    public void setMainView() {
        //cleanMusicFileDir();
        getSongsForActivity(); 
        SongAdapter songListAdapter = new SongAdapter(this, songInfoList);
        songView.setAdapter(songListAdapter);
    }

    public void getSongsForActivity() {

        // Get database and song list
        SongDBHandler db = new SongDBHandler(this);
        songInfoList = db.getSongDataList();
    }

    // Not going to use for now, but could be useful later on
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.d(TAG, "Service is running.");
                return true;
            }
        }
        Log.d(TAG, "Service is not running.");
        return false;
    }
}
