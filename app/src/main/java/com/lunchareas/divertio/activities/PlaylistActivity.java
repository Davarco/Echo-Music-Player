package com.lunchareas.divertio.activities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.adapters.SongFixedAdapter;
import com.lunchareas.divertio.fragments.AddSongsToPlaylistDialog;
import com.lunchareas.divertio.fragments.ChangePlaylistTitleDialog;
import com.lunchareas.divertio.fragments.DeleteSongsFromPlaylistDialog;
import com.lunchareas.divertio.models.PlaylistDBHandler;
import com.lunchareas.divertio.models.PlaylistData;
import com.lunchareas.divertio.models.SongData;
//https://www.youtube.com/watch?v=1UlRIbpYTwk
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PlaylistActivity extends BasePlayerActivity {

    private static final String TAG = PlaylistActivity.class.getName();

    private List<SongData> playlistSongList;
    private ListView playlistView;
    private ImageView playButton;
    private RelativeLayout playlistBackground;
    private PlaylistData playlistData;
    private TextView playlistViewName;
    private int position;

    public PlaylistActivity() {
        super(R.layout.activity_playlist);
    }

    @Override
    protected void initToolbar() {

        // Get toolbar
        mainBar = (Toolbar) findViewById(R.id.main_bar);
        setSupportActionBar(mainBar);

        // Add back icon
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
    }

    @Override
    protected void initViews() {

        // Init views
        playlistSongList = new ArrayList<>();
        playlistView = (ListView) findViewById(R.id.song_list);
        playlistBackground = (RelativeLayout) findViewById(R.id.playlist_background);

        // Large red button
        playButton = (ImageView) findViewById(R.id.playlist_play_button);

        // Large name
        playlistViewName = (TextView) findViewById(R.id.playlist_name);

        // Get play button
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songCtrlButton.setImageResource(R.drawable.ic_pause);
                sendMusicPauseIntent();
                sendListCreateIntent(playlistData.getSongList());
                musicBound = true;
            }
        });

        // Just for feeling
        playlistView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //showChoiceMenu(view, position);
                return true;
            }
        });
    }

    @Override
    protected void initSongbar() {

        // Setup song bar
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        songProgressManager = (SeekBar) findViewById(R.id.progress_bar);
        songProgressManager.getProgressDrawable().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        songProgressManager.getThumb().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        songCtrlButton = (ImageView) findViewById(R.id.play_button);
        musicBound = true;

        // Setup play button
        songCtrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicBound) {
                    sendMusicPauseIntent();
                    songCtrlButton.setImageResource(R.drawable.ic_play);
                } else {
                    sendMusicStartIntent();
                    songCtrlButton.setImageResource(R.drawable.ic_pause);
                }
            }
        });
    }

    @Override
    protected void getDispData() {

        // Get playlist data
        if (getIntent().getExtras() == null) {
            Log.e(TAG, "Extras were not passed to playlist manager.");
        }
        String playlistName = getIntent().getStringExtra(PlaylistMenuActivity.PLAYLIST_NAME);
        PlaylistDBHandler db = new PlaylistDBHandler(this);
        playlistData = db.getPlaylistData(playlistName);
        playlistSongList = playlistData.getSongList();
        position = playlistInfoList.indexOf(playlistData);
    }

    @Override
    protected void updateDispData() {

        // Get the new playlist data
        playlistData = getPlaylistInfoList().get(position);
        playlistSongList = playlistData.getSongList();
    }

    @Override
    protected void showDispData() {

        // Set opaque background
        playlistBackground.getBackground().setAlpha(150);

        // Add playlist name
        playlistViewName.setText(playlistData.getPlaylistName());

        // Set adapter for list
        SongFixedAdapter songListAdapter = new SongFixedAdapter(this, playlistSongList);
        playlistView.setAdapter(songListAdapter);

        // Set correct background color
        if (playlistSongList.size() % 2 - 1 == 0) {
            findViewById(R.id.activity).setBackgroundResource(R.color.gray_2);
        } else {
            findViewById(R.id.activity).setBackgroundResource(R.color.gray_3);
        }

        // Set center icon image
        if (playlistBackground != null) {
            if (playlistData.getPlaylistIcon() == null) {

                // Try with song icon
                List<SongData> songList = playlistData.getSongList();
                Collections.shuffle(songList);
                boolean found = false;
                for (SongData songData : songList) {
                    if (songData.getSongCover() != null) {
                        playlistBackground.setBackground(songData.getSongCover());
                        found = true;
                    }
                }

                // Songs had no icon too
                if (!found) {
                    Drawable drawable = getResources().getDrawable(R.drawable.ic_media_icon);
                    playlistBackground.setBackground(drawable);
                }

            } else {
                playlistBackground.setBackground(playlistData.getPlaylistIcon());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.playlist_overflow_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                Intent i = new Intent(this, PlaylistMenuActivity.class);
                startActivity(i);
                finish();
                return true;
            }
            case R.id.playlist_rename: {
                // Create popup for new title
                DialogFragment dialogFragment = new ChangePlaylistTitleDialog();
                Bundle bundle = new Bundle();
                bundle.putString(ChangePlaylistTitleDialog.MUSIC_POS, playlistData.getPlaylistName());
                dialogFragment.setArguments(bundle);
                dialogFragment.show(getSupportFragmentManager(), "ChangePlaylistTitle");
                return true;
            }
            case R.id.playlist_add_music: {
                // Create popup to add music
                DialogFragment dialogFragment = new AddSongsToPlaylistDialog();
                Bundle bundle = new Bundle();
                bundle.putString(AddSongsToPlaylistDialog.MUSIC_POS, playlistData.getPlaylistName());
                dialogFragment.setArguments(bundle);
                dialogFragment.show(getSupportFragmentManager(), "AddMusicTitle");
                return true;
            }
            case R.id.playlist_delete_music: {
                // Create popup to delete music
                DialogFragment dialogFragment = new DeleteSongsFromPlaylistDialog();
                Bundle bundle = new Bundle();
                bundle.putString(AddSongsToPlaylistDialog.MUSIC_POS, playlistData.getPlaylistName());
                dialogFragment.setArguments(bundle);
                dialogFragment.show(getSupportFragmentManager(), "DeleteMusicTitle");
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
