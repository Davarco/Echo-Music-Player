package com.lunchareas.echomp.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageSwitcher;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lunchareas.echomp.R;
import com.lunchareas.echomp.adapters.QueueAdapter;
import com.lunchareas.echomp.adapters.QueueSelectionAdapter;
import com.lunchareas.echomp.dataloaders.SongLoader;
import com.lunchareas.echomp.dialogs.PlaylistAddSongsDialog;
import com.lunchareas.echomp.dialogs.PlaylistFromSongsDialog;
import com.lunchareas.echomp.models.Song;
import com.lunchareas.echomp.services.MediaService;
import com.lunchareas.echomp.utils.Constants;
import com.lunchareas.echomp.utils.MediaControlUtils;
import com.lunchareas.echomp.utils.NavUtils;
import com.lunchareas.echomp.utils.ShareUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class QueueActivity extends BaseActivity {

    private static final String TAG = QueueActivity.class.getName();
    private final Activity activity = this;

    private SeekBar progressBar;
    private ImageSwitcher songPlayButton;
    private ListView listView;
    private List<Song> songList;
    private Handler handler;
    private QueueAdapter queueAdapter;
    private QueueSelectionAdapter selectionAdapter;

    private boolean musicBound;
    private boolean isChanging;
    private int currSize;

    public QueueActivity() {
        super(R.layout.activity_queue);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the data
        getDispData();

        // Setup songbar
        initSongbar();

        // Setup content
        initMainView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDispData();
    }

    @SuppressLint("NewApi")
    private void initSongbar() {

        // Get the song bar
        progressBar = (SeekBar) findViewById(R.id.songbar_progress);
        progressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        progressBar.getThumb().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        songPlayButton = (ImageSwitcher) findViewById(R.id.songbar_play);
        songPlayButton.setBackgroundResource(R.drawable.ic_pause);
        musicBound = true;

        // Add play button listener
        songPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicBound) {
                    MediaControlUtils.pause(activity);
                    songPlayButton.setBackgroundResource(R.drawable.ic_play);
                } else {
                    MediaControlUtils.start(activity);
                    songPlayButton.setBackgroundResource(R.drawable.ic_pause);
                }
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
                songPlayButton.setBackgroundResource(R.drawable.ic_pause);
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
                            songPlayButton.setBackgroundResource(R.drawable.ic_pause);
                        } else {
                            songPlayButton.setBackgroundResource(R.drawable.ic_play);
                        }
                    }
                    musicBound = status;

                    // Update if different song or change in size
                    long tempSong = MediaService.getCurrSong();
                    int size = MediaService.getSongList().size();
                    if (tempSong != currSong) {
                        currSong = tempSong;
                        updateDispData();
                    } else if (size != currSize) {
                        currSize = size;
                        queueAdapter = new QueueAdapter(activity, songList);
                        listView.setAdapter(queueAdapter);
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

    private void initMainView() {

        // Set new title
        QueueActivity.this.setTitle("Playing Queue");

        // Initiate views
        listView = (ListView) findViewById(R.id.songlist);
        if (songList != null) {

            // Get the list view
            queueAdapter = new QueueAdapter(activity, songList);
            listView.setAdapter(queueAdapter);

            // Set multi-choice listener
            selectionAdapter = new QueueSelectionAdapter(activity, R.layout.fragment_song, songList);
            listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
            listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
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
                    inflater.inflate(R.menu.selection_menu_queue, menu);
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
                        case R.id.remove_from_queue: {
                            List<Integer> temp = selectionAdapter.getSelectedSongs();
                            List<Song> selected = new ArrayList<>();
                            for (Integer idx: temp) {
                                selected.add(songList.get(idx));
                            }
                            MediaControlUtils.removeFromQueue(activity, selected);
                            mode.finish();
                            return true;
                        }
                        case R.id.add_to_playlist: {
                            List<Integer> temp = selectionAdapter.getSelectedSongs();
                            List<Song> selected = new ArrayList<>();
                            for (Integer idx: temp) {
                                selected.add(songList.get(idx));
                            }
                            Bundle bundle = new Bundle();
                            bundle.putParcelableArrayList(Constants.SONG_LIST, (ArrayList<Song>) selected);
                            DialogFragment dialogFragment = new PlaylistAddSongsDialog();
                            dialogFragment.setArguments(bundle);
                            dialogFragment.show(activity.getFragmentManager(), "AddToPlaylist");
                            mode.finish();
                            return true;
                        }
                        case R.id.create_playlist: {
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
                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    selectionAdapter.resetSelection();
                    int index = listView.getFirstVisiblePosition();
                    View v = listView.getChildAt(0);
                    int top = (v == null) ? 0 : (v.getTop() - listView.getPaddingTop());
                    listView.setAdapter(new QueueAdapter(activity, songList));
                    listView.setSelectionFromTop(index, top);
                    ((QueueActivity) activity).getSupportActionBar().show();
                }
            });

            // Set background based on size
            if (songList.size()%2 == 0) {
                listView.setBackgroundResource(R.color.gray_darker);
            } else {
                listView.setBackgroundResource(R.color.gray);
            }
        }
    }

    private void getDispData() {
        songList = MediaService.getSongList();
        if (songList != null)
            currSize = songList.size();
        else
            currSize = 0;
    }

    private void updateDispData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (listView != null && listView.getAdapter() != null)
                    ((QueueAdapter) listView.getAdapter()).updateContent();
            }
        });
    }

    @Override
    protected void selectNavItem(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.library: {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            case R.id.queue: {
                Intent intent = new Intent(this, QueueActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            case R.id.now_playing: {
                Intent intent = new Intent(this, MusicPlayerActivity.class);
                intent.putExtra(Constants.MUSIC_ID, currSong);
                startActivity(intent);
                finish();
                break;
            }
            case R.id.equalizer: {
                NavUtils.goToEqualizer(activity);
                finish();
                break;
            }
            case R.id.info: {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Echo Music v1.3");
                builder.setMessage("\nEcho Music is an MP3 player with built-in video-to-mp3 conversion, developed by Echo Labs. Logo icon by icons8.net. \n\n" +
                        "Please report any bugs to echomusiclabs@gmail.com.\n\n" +
                        "Thank you for using Echo Music!\n");
                builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow_menu_queue, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.play_all: {
                List<Song> temp = new ArrayList<>(songList);
                MediaControlUtils.startQueueRepeatingShuffled(activity, temp);
                return true;
            }
            case R.id.a_to_z: {
                Collections.sort(songList, new AlphabeticalComparator());
                MediaService.setSongList(songList);
                MediaService.setIdx(MediaControlUtils.findIdxById(currSong));
                updateDispData();
                return true;
            }
            case R.id.z_to_a: {
                Collections.sort(songList, new ReverseAlphabeticalComparator());
                MediaService.setSongList(songList);
                MediaService.setIdx(MediaControlUtils.findIdxById(currSong));
                updateDispData();
                return true;
            }
            case R.id.album_name: {
                Collections.sort(songList, new AlbumNameComparator());
                MediaService.setSongList(songList);
                MediaService.setIdx(MediaControlUtils.findIdxById(currSong));
                updateDispData();
                return true;
            }
            case R.id.artist_name: {
                Collections.sort(songList, new ArtistNameComparator());
                MediaService.setSongList(songList);
                MediaService.setIdx(MediaControlUtils.findIdxById(currSong));
                updateDispData();
                return true;
            }
            case R.id.duration: {
                Collections.sort(songList, new DurationComparator());
                MediaService.setSongList(songList);
                MediaService.setIdx(MediaControlUtils.findIdxById(currSong));
                updateDispData();
                return true;
            }
            case R.id.share: {
                ShareUtils.shareTrackList(activity, songList );
                return true;
            }
            case R.id.equalizer: {
                NavUtils.goToEqualizer(activity);
                return true;
            }
            case R.id.go_to_artist: {
                Song temp = SongLoader.findSongById(currSong);
                assert temp != null;
                NavUtils.goToArtist(activity, temp.getArtistId());
                finish();
                return true;
            }
            case R.id.go_to_album: {
                Song temp = SongLoader.findSongById(currSong);
                assert temp != null;
                NavUtils.goToAlbum(activity, temp.getAlbumId());
                finish();
                return true;
            }
        }

        return false;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onStart() {
        super.onStart();
        menuDrawer.closeDrawers();
    }

    @Override
    protected void onStop() {
        super.onStop();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
