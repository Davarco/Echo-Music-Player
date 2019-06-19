package com.lunchareas.echomp.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lunchareas.echomp.R;
import com.lunchareas.echomp.adapters.PlaylistSelectionAdapter;
import com.lunchareas.echomp.adapters.SongPlainAdapter;
import com.lunchareas.echomp.dataloaders.PlaylistLoader;
import com.lunchareas.echomp.models.Playlist;
import com.lunchareas.echomp.models.Song;
import com.lunchareas.echomp.utils.Constants;
import com.lunchareas.echomp.utils.MediaControlUtils;
import com.lunchareas.echomp.utils.MediaDataUtils;
import com.lunchareas.echomp.utils.NavUtils;
import com.lunchareas.echomp.utils.ShareUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;

import java.util.ArrayList;
import java.util.List;


public class PlaylistPlayerActivity extends ListPlayerActivity {

    private static final String TAG = PlaylistPlayerActivity.class.getName();
    private final Activity activity = this;

    private Playlist playlist;
    private PlaylistSelectionAdapter selectionAdapter;

    private boolean isUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceSate) {
        super.onCreate(savedInstanceSate);

        // Setup multi choice listener
        initMultiChoice();
    }

    private void initMultiChoice() {

        // Set multi choice listener
        selectionAdapter = new PlaylistSelectionAdapter(activity, R.layout.item_playlist_song_selected, songList);
        if (playlist.getId() > 0) {
            songListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            songListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                    // Change the title to num of clicked items
                    int numChecked = songListView.getCheckedItemCount();
                    mode.setTitle(numChecked + " Selected");
                    selectionAdapter.toggleSelection(position);
                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    // Create the menu for the overflow
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.selection_menu_playlist, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    // Set colored and hide bar
                    int index = songListView.getFirstVisiblePosition();
                    View v = songListView.getChildAt(0);
                    int top = (v == null) ? 0 : (v.getTop() - songListView.getPaddingTop());
                    songListView.setAdapter(selectionAdapter);
                    songListView.setSelectionFromTop(index, top);
                    return true;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.delete: {
                            List<Integer> temp = selectionAdapter.getSelectedSongs();
                            List<Long> selected = new ArrayList<>();
                            for (Integer idx : temp) {
                                selected.add(songList.get(idx).getId());
                            }
                            MediaDataUtils.deletePlaylistSongs(playlist.getId(), selected, activity);
                            updateDispData();
                            mode.finish();
                            return true;
                        }
                        case R.id.add_to_queue: {
                            List<Integer> temp = selectionAdapter.getSelectedSongs();
                            List<Song> selected = new ArrayList<>();
                            for (Integer idx : temp) {
                                selected.add(songList.get(idx));
                            }
                            MediaControlUtils.addToQueue(activity, selected);
                            return true;
                        }
                        case R.id.play_all: {
                            // Add songs to queue
                            List<Integer> selected = selectionAdapter.getSelectedSongs();
                            List<Song> tempList = new ArrayList<>();
                            for (Integer integer : selected) {
                                tempList.add(songList.get(integer));
                            }
                            MediaControlUtils.startQueueShuffled(activity, tempList);
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    selectionAdapter.resetSelection();
                    int index = songListView.getFirstVisiblePosition();
                    View v = songListView.getChildAt(0);
                    int top = (v == null) ? 0 : (v.getTop() - songListView.getPaddingTop());
                    songListView.setAdapter(new SongPlainAdapter(activity, songList));
                    songListView.setSelectionFromTop(index, top);
                    ((PlaylistPlayerActivity) activity).getSupportActionBar().show();
                }
            });
        }
    }

    @Override
    protected void getDispData() {

        // Get data model
        long id = getIntent().getLongExtra(Constants.PLAYLIST_ID, 0);
        playlist = PlaylistLoader.findPlaylistById(id);
    }

    @Override
    protected void showDispData() {

        // Extract data onto views
        if (playlist != null) {

            // Set name and song list
            listName.setText(playlist.getName());
            songList = MediaDataUtils.getSongsFromPlaylist(playlist.getId(), activity);

            // Songs have to exist
            if (songList.size() > 0) {

                // Set cover using a random song
                Song song = songList.get((int) (Math.random() * songList.size()));
                if (song != null) {
                    Glide.with(activity)
                            .load(MediaDataUtils.getAlbumArt(song.getAlbumId()))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(containerBackground);
                }

                // Populate song list view
                songListView.setAdapter(new SongPlainAdapter(activity, songList));
                if (songList.size() % 2 == 0) {
                    containerBackground.setBackgroundResource(R.color.gray_darker);
                } else {
                    containerBackground.setBackgroundResource(R.color.gray);
                }
            } else {
                Glide.with(activity)
                        .load(R.drawable.nav_background)
                        .into(containerBackground);
            }
        }
    }

    @Override
    protected void updateDispData() {

        // Re-get playlist
        isUpdated = true;
        List<Playlist> playlists = PlaylistLoader.getPlaylistList(activity);
        for (Playlist p: playlists) {
            if (p.getId() == playlist.getId()) {
                playlist = p;
                songList = MediaDataUtils.getSongsFromPlaylist(playlist.getId(), activity);
            }
        }

        // Songs have to exist
        if (songList.size() > 0) {

            // Set cover using a random song
            Song song = songList.get((int) (Math.random() * songList.size()));
            Glide.with(activity)
                    .load(MediaDataUtils.getAlbumArt(song.getAlbumId()))
                    .into(containerBackground);

            // Populate song list view
            songListView.setAdapter(new SongPlainAdapter(activity, songList));
            if (songList.size() % 2 == 0) {
                containerBackground.setBackgroundResource(R.color.gray_darker);
            } else {
                containerBackground.setBackgroundResource(R.color.gray);
            }

        } else {
            Glide.with(activity)
                    .load(R.drawable.nav_background)
                    .into(containerBackground);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                if (isUpdated) {
                    Intent intent = new Intent();
                    intent.putExtra(Constants.RESULT_DEFAULT, true);
                    setResult(RESULT_OK, intent);
                } else {
                    Intent intent = new Intent();
                    intent.putExtra(Constants.RESULT_DEFAULT, false);
                    setResult(RESULT_OK, intent);
                }
                finish();
                break;
            }
            case R.id.share: {
                ShareUtils.shareTrackList(activity, songList);
                break;
            }
            case R.id.equalizer: {
                NavUtils.goToEqualizer(activity);
                return true;
            }
            case R.id.search: {
                NavUtils.goToSearch(activity);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onDestroy() {
        PicassoTools.clearCache(Picasso.with(this));
        System.gc();
        super.onDestroy();
    }
}
