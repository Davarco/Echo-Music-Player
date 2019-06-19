package com.lunchareas.echomp.fragments;


import android.app.Activity;
import android.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lunchareas.echomp.R;
import com.lunchareas.echomp.activities.MainActivity;
import com.lunchareas.echomp.adapters.SongAdapter;
import com.lunchareas.echomp.adapters.SongSelectionAdapter;
import com.lunchareas.echomp.dialogs.PlaylistAddSongsDialog;
import com.lunchareas.echomp.dialogs.PlaylistFromSongsDialog;
import com.lunchareas.echomp.dialogs.SongDownloadDialog;
import com.lunchareas.echomp.models.Song;
import com.lunchareas.echomp.utils.Constants;
import com.lunchareas.echomp.utils.MediaControlUtils;
import com.lunchareas.echomp.utils.MediaDataUtils;
import com.lunchareas.echomp.utils.NavUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SongFragment extends Fragment {

    private static final String TAG = SongFragment.class.getName();

    private List<Song> songList;
    private View libraryView;
    private ListView listView;
    private SongSelectionAdapter selectionAdapter;
    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Get the data
        activity = getActivity();
        songList = ((MainActivity) activity).getSongList();
        libraryView = inflater.inflate(R.layout.fragment_song, container, false);

        // Set the list view to the songs
        listView = (ListView) libraryView.findViewById(R.id.songlist);
        listView.setAdapter(new SongAdapter(activity, songList));

        // Set single choice mode listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaControlUtils.initRepeating(activity, songList, position);
                ((MainActivity) activity).setMusicBound(true);
                NavUtils.goToSong(activity, songList.get(position).getId());
            }
        });

        // Set modal choice listener
        selectionAdapter = new SongSelectionAdapter(activity, R.layout.item_song, songList);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new ListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // Change the title to num of clicked items
                int numChecked = listView.getCheckedItemCount();
                mode.setTitle(numChecked + " Selected");
                selectionAdapter.toggleSelection(position);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Create the menu for the overflow
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.selection_menu_song, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Set colored and hide bar
                int index = listView.getFirstVisiblePosition();
                View v = listView.getChildAt(0);
                int top = (v == null) ? 0 : (v.getTop() - listView.getPaddingTop());
                listView.setAdapter(selectionAdapter);
                listView.setSelectionFromTop(index, top);
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete: {
                        // Delete the selected songs
                        List<Integer> selected = selectionAdapter.getSelectedSongs();
                        List<Song> tempList = new ArrayList<>();
                        for (Integer integer: selected) {
                            tempList.add(songList.get(integer));
                        }
                        MediaDataUtils.deleteSongList(tempList, activity);
                        ((MainActivity) activity).updateAll();
                        mode.finish();
                        return true;
                    }
                    case R.id.create_playlist: {
                        // Create a playlist with the songs
                        List<Integer> selected = selectionAdapter.getSelectedSongs();
                        List<Song> tempList = new ArrayList<>();
                        for (Integer integer: selected) {
                            tempList.add(songList.get(integer));
                        }
                        Bundle bundle = new Bundle();
                        bundle.putParcelableArrayList(Constants.SONG_LIST, (ArrayList<Song>) tempList);
                        DialogFragment dialogFragment = new PlaylistFromSongsDialog();
                        dialogFragment.setArguments(bundle);
                        dialogFragment.show(activity.getFragmentManager(), "PlaylistFromSongs");
                        mode.finish();
                        return true;
                    }
                    case R.id.add_to_playlist: {
                        // Add songs to playlists
                        List<Integer> selected = selectionAdapter.getSelectedSongs();
                        List<Song> tempList = new ArrayList<>();
                        for (Integer integer: selected) {
                            tempList.add(songList.get(integer));
                        }
                        Bundle bundle = new Bundle();
                        bundle.putParcelableArrayList(Constants.SONG_LIST, (ArrayList<Song>) tempList);
                        DialogFragment dialogFragment = new PlaylistAddSongsDialog();
                        dialogFragment.setArguments(bundle);
                        dialogFragment.show(activity.getFragmentManager(), "AddToPlaylist");
                        mode.finish();
                        return true;
                    }
                    case R.id.add_to_queue: {
                        // Add songs to queue
                        List<Integer> selected = selectionAdapter.getSelectedSongs();
                        List<Song> tempList = new ArrayList<>();
                        for (Integer integer: selected) {
                            tempList.add(songList.get(integer));
                        }
                        MediaControlUtils.addToQueue(activity, tempList);
                        mode.finish();
                        return true;
                    }
                    case R.id.play_all: {
                        // Add songs to queue
                        List<Integer> selected = selectionAdapter.getSelectedSongs();
                        List<Song> tempList = new ArrayList<>();
                        for (Integer integer: selected) {
                            tempList.add(songList.get(integer));
                        }
                        MediaControlUtils.startQueueShuffled(activity, tempList);
                        mode.finish();
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                selectionAdapter.resetSelection();
                int index = listView.getFirstVisiblePosition();
                View v = listView.getChildAt(0);
                int top = (v == null) ? 0 : (v.getTop() - listView.getPaddingTop());
                listView.setAdapter(new SongAdapter(activity, songList));
                listView.setSelectionFromTop(index, top);
                ((MainActivity) activity).getSupportActionBar().show();
            }
        });

        // Set background based on size
        if (songList.size()%2 == 0) {
            libraryView.findViewById(R.id.fragment).setBackgroundResource(R.color.gray_darker);
        } else {
            libraryView.findViewById(R.id.fragment).setBackgroundResource(R.color.gray);
        }

        // Unique options menu
        setHasOptionsMenu(true);

        return libraryView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.overflow_menu_song, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search: {
                NavUtils.goToSearch(activity);
                return true;
            }
            case R.id.play_all: {
                List<Song> temp = new ArrayList<>(songList);
                MediaControlUtils.startQueueRepeatingShuffled(activity, temp);
                return true;
            }
            case R.id.a_to_z: {
                Collections.sort(((MainActivity) activity).getSongList(), new AlphabeticalComparator());
                ((MainActivity) activity).updateContent();
                return true;
            }
            case R.id.z_to_a: {
                Collections.sort(((MainActivity) activity).getSongList(), new ReverseAlphabeticalComparator());
                ((MainActivity) activity).updateContent();
                return true;
            }
            case R.id.album_name: {
                Collections.sort(((MainActivity) activity).getSongList(), new AlbumNameComparator());
                ((MainActivity) activity).updateContent();
                return true;
            }
            case R.id.artist_name: {
                Collections.sort(((MainActivity) activity).getSongList(), new ArtistNameComparator());
                ((MainActivity) activity).updateContent();
                return true;
            }
            case R.id.duration: {
                Collections.sort(((MainActivity) activity).getSongList(), new DurationComparator());
                ((MainActivity) activity).updateContent();
                return true;
            }
            case R.id.download: {
                DialogFragment dialogFragment = new SongDownloadDialog();
                dialogFragment.show(activity.getFragmentManager(), "Download");
                return true;
            }
            case R.id.equalizer: {
                NavUtils.goToEqualizer(activity);
                return true;
            }
        }

        return false;
    }

    private class AlphabeticalComparator implements Comparator<Song> {
        @Override
        public int compare(Song song1, Song song2) {
            String name1 = song1.getName().replaceAll("[^\\p{L}\\p{Nd}]+", "");
            String name2 = song2.getName().replaceAll("[^\\p{L}\\p{Nd}]+", "");
            return name1.compareTo(name2);
        }
    }

    private class ReverseAlphabeticalComparator implements Comparator<Song> {
        @Override
        public int compare(Song song1, Song song2) {
            String name1 = song1.getName().replaceAll("[^\\p{L}\\p{Nd}]+", "");
            String name2 = song2.getName().replaceAll("[^\\p{L}\\p{Nd}]+", "");
            return name2.compareTo(name1);
        }
    }

    private class AlbumNameComparator implements Comparator<Song> {
        @Override
        public int compare(Song song1, Song song2) {
            String name1 = song1.getAlbum().replaceAll("[^\\p{L}\\p{Nd}]+", "");
            String name2 = song2.getAlbum().replaceAll("[^\\p{L}\\p{Nd}]+", "");
            return name1.compareTo(name2);
        }
    }

    private class ArtistNameComparator implements Comparator<Song> {
        @Override
        public int compare(Song song1, Song song2) {
            String name1 = song1.getArtist().replaceAll("[^\\p{L}\\p{Nd}]+", "");
            String name2 = song2.getArtist().replaceAll("[^\\p{L}\\p{Nd}]+", "");
            return name1.compareTo(name2);
        }
    }

    private class DurationComparator implements Comparator<Song> {
        @Override
        public int compare(Song song1, Song song2) {
            return song1.getDuration() - song2.getDuration();
        }
    }
}
