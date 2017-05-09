package com.lunchareas.divertio.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.lunchareas.divertio.adapters.PlaylistSelectionAdapter;
import com.lunchareas.divertio.fragments.AddSongsToPlaylistDialog;
import com.lunchareas.divertio.fragments.ChangePlaylistTitleDialog;
import com.lunchareas.divertio.fragments.CreatePlaylistDialog;
import com.lunchareas.divertio.fragments.CreatePlaylistNameFailureDialog;
import com.lunchareas.divertio.fragments.DeletePlaylistDialog;
import com.lunchareas.divertio.adapters.PlaylistAdapter;
import com.lunchareas.divertio.fragments.DeleteSongsFromPlaylistDialog;
import com.lunchareas.divertio.models.PlaylistDBHandler;
import com.lunchareas.divertio.models.PlaylistData;
import com.lunchareas.divertio.R;
import com.lunchareas.divertio.utils.PlaylistQueueUtil;
import com.lunchareas.divertio.utils.PlaylistUtil;

import java.util.ArrayList;
import java.util.List;

public class PlaylistActivity extends BaseActivity {

    private static final String TAG = PlaylistActivity.class.getName();

    public static final String PLAYLIST_NAME = "playlist_name";

    private int currentPosition;
    private ListView playlistView;
    private PlaylistSelectionAdapter selectionAdapter;

    public PlaylistActivity() {
        super(R.layout.activity_playlist);
    }

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create playlist
        playlistView = (ListView) findViewById(R.id.playlist_list);
        setMainView();

        // Create the selection adapter
        if (playlistInfoList == null) {
            Log.d(TAG, "No playlist list found yet.");
        }
        selectionAdapter = new PlaylistSelectionAdapter(this, R.layout.playlist_layout, playlistInfoList);

        // Current position is -1 because no playlist is playing
        currentPosition = -1;

        playlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Log.d(TAG, "Detected click in playlist item in list view, starting modifier.");
                Intent i = new Intent(view.getContext(), PlaylistControllerActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                i.putExtra(PLAYLIST_NAME, playlistInfoList.get(position).getPlaylistName());
                startActivity(i);
            }
        });

        playlistView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Detected LONG click on playlist.");
                showChoiceMenu(view, position);
                return true;
            }
        });

        /*
        // Set new mode and add listener
        playlistView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        playlistView.setMultiChoiceModeListener(new ListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // Change the title to num of clicked items
                Log.d(TAG, "Playlist item checked state changed.");
                int numChecked = playlistView.getCheckedItemCount();
                mode.setTitle(numChecked + " Selected");
                selectionAdapter.toggleSelection(position);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Create the menu for the overflow
                Log.d(TAG, "Creating playlist action mode.");
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.playlist_selection_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Set colored and hide bar
                Log.d(TAG, "Preparing playlist action mode.");
                playlistView.setAdapter(selectionAdapter);
                getSupportActionBar().hide();
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                Log.d(TAG, "Playlist action item clicked.");
                switch (item.getItemId()) {

                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                Log.d(TAG, "Playlist action mode destroyed.");
                selectionAdapter.resetSelection();
                getSupportActionBar().show();
            }
        });
        */
    }

    @SuppressLint("NewApi")
    public void showChoiceMenu(View view, final int pos) {
        final PopupMenu popupMenu = new PopupMenu(context, view, Gravity.END);
        final PlaylistData playlistData = playlistInfoList.get(pos);

        // Handle individual clicks
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.playlist_rename_title: {
                        Log.d(TAG, "Renaming playlist.");

                        // Create popup for new playlist title
                        DialogFragment changePlaylistTitleDialog = new ChangePlaylistTitleDialog();
                        Bundle bundle = new Bundle();
                        bundle.putInt(ChangePlaylistTitleDialog.MUSIC_POS, pos);
                        changePlaylistTitleDialog.setArguments(bundle);
                        changePlaylistTitleDialog.show(getSupportFragmentManager(), "ChangePlaylistTitle");

                        return true;
                    }
                    case R.id.playlist_remove_title: {
                        Log.d(TAG, "Deleting playlist.");

                        // Create popup for remove playlist title
                        playlistUtil = new PlaylistUtil(context);
                        playlistUtil.deletePlaylist(playlistData);
                        setMainView();

                        return true;
                    }
                    case R.id.playlist_add_music_title: {
                        Log.d(TAG, "Adding music to playlist.");

                        // Create popup for music to add
                        DialogFragment addSongsDialog = new AddSongsToPlaylistDialog();
                        Bundle bundle = new Bundle();
                        bundle.putInt(AddSongsToPlaylistDialog.MUSIC_POS, pos);
                        addSongsDialog.setArguments(bundle);
                        addSongsDialog.show(getSupportFragmentManager(), "AddSongsToPlaylist");

                        return true;
                    }
                    case R.id.playlist_delete_music_title: {
                        Log.d(TAG, "Deleting music from playlist.");

                        // Create popup for music to delete
                        DialogFragment removeSongsDialog = new DeleteSongsFromPlaylistDialog();
                        Bundle bundle = new Bundle();
                        bundle.putInt(DeleteSongsFromPlaylistDialog.MUSIC_POS, pos);
                        removeSongsDialog.setArguments(bundle);
                        removeSongsDialog.show(getSupportFragmentManager(), "RemoveSongsFromPlaylist");

                        return true;
                    }
                    case R.id.playlist_play_next: {
                        Log.d(TAG, "Playing this playlist.");

                        // Get random first num
                        int firstSong = (int)(Math.random()*playlistData.getSongList().size());

                        // Create queue controller to run
                        PlaylistQueueUtil queueManager = new PlaylistQueueUtil(firstSong, playlistData, context);
                        queueManager.startQueue();
                        songCtrlButton.setBackgroundResource(R.drawable.pause_red);
                    }
                    default: {
                        return false;
                    }
                }
            }
        });

        // Show popup menu
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.playlist_choice_menu, popupMenu.getMenu());
        popupMenu.show();
    }

    @Override
    protected void setDisplay() {
        setContentView(R.layout.activity_playlist);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.playlist_overflow_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "Detected that position " + item.getItemId() + " was selected.");
        switch (item.getItemId()) {
            case R.id.playlist_menu_create: {
                Log.d(TAG, "Starting new dialog - upload.");
                DialogFragment createPlaylistDialog = new CreatePlaylistDialog();
                createPlaylistDialog.show(getSupportFragmentManager(), "Upload");
                return true;
            }
            case R.id.playlist_menu_delete: {
                Log.d(TAG, "Starting new dialog - delete.");
                DialogFragment deletePlaylistDialog = new DeletePlaylistDialog();
                deletePlaylistDialog.show(getSupportFragmentManager(), "Delete");
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
                Log.d(TAG, "Starting new activity - main.");
                Intent i = new Intent(this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                break;
            }
            case 1: {
                Log.d(TAG, "No effect, on that activity.");
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
        Log.d(TAG, "Setting main view.");
        updatePlaylistInfoList();
        PlaylistAdapter playlistAdapter = new PlaylistAdapter(this, playlistInfoList);
        playlistView.setAdapter(playlistAdapter);
    }

    public List<PlaylistData> getPlaylistInfoList() {
        return this.playlistInfoList;
    }
}
