package com.lunchareas.echomp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lunchareas.echomp.R;
import com.lunchareas.echomp.services.MediaService;
import com.lunchareas.echomp.utils.NavUtils;
import com.lunchareas.echomp.widgets.FontTabLayout;
import com.lunchareas.echomp.adapters.SlidePagerAdapter;
import com.lunchareas.echomp.dataloaders.AlbumLoader;
import com.lunchareas.echomp.dataloaders.ArtistLoader;
import com.lunchareas.echomp.models.Album;
import com.lunchareas.echomp.models.Artist;
import com.lunchareas.echomp.models.Playlist;
import com.lunchareas.echomp.models.Song;
import com.lunchareas.echomp.dataloaders.PlaylistLoader;
import com.lunchareas.echomp.dataloaders.SongLoader;
import com.lunchareas.echomp.utils.Constants;
import com.lunchareas.echomp.utils.MediaControlUtils;
import com.lunchareas.echomp.utils.MediaDataUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getName();
    private final Activity activity = this;

    private SeekBar progressBar;
    private ImageSwitcher songPlayButton;
    private ViewPager viewPager;
    private Thread getSongs;
    private Thread getPlaylists;
    private Thread getAlbums;
    private Thread getArtists;
    private List<Song> songList;
    private List<Playlist> playlistList;
    private List<Album> albumList;
    private List<Artist> artistList;
    private Handler handler;

    private boolean isChanging;
    private boolean musicBound;
    private long albumId;

    public MainActivity() {
        super(R.layout.activity_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // External storage permissions
        getPermissions();

        // Get data
        getData();

        // Setup player
        initPlayer();

        // Initial fragment
        waitForData();
        initMainView();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Close drawers if open
        menuDrawer.closeDrawers();

        // Setup songbar
        initSongbar();
    }

    private void getPermissions() {
        if (Build.VERSION.SDK_INT < 23) {
            Log.d(TAG, "Don't need permissions.");
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Log.d(TAG, "PERMISSIONS: App needs permissions to read external storage.");
                }
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            }
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Log.d(TAG, "PERMISSIONS: App needs permissions to read external storage.");
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    private void getData() {

        // Multi-thread for performance
        getSongs = new Thread(new Runnable() {
            @Override
            public void run() {
                songList = SongLoader.getSongList(activity);
            }
        });
        getPlaylists = new Thread(new Runnable() {
            @Override
            public void run() {
                playlistList = PlaylistLoader.getPlaylistList(activity);
            }
        });
        getAlbums = new Thread(new Runnable() {
            @Override
            public void run() {
                albumList = AlbumLoader.getAlbumList(activity);
            }
        });
        getArtists = new Thread(new Runnable() {
            @Override
            public void run() {
                artistList = ArtistLoader.getArtistList(activity);
            }
        });
        getSongs.start();
        getPlaylists.start();
        getAlbums.start();
        getArtists.start();
    }

    private void initPlayer() {
        MediaControlUtils.initController(this);
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

                    // Update if different song
                    long tempSong = MediaService.getCurrSong();
                    if (tempSong != currSong) {
                        currSong = tempSong;
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

                } catch (Exception ignored) {}

                handler.postDelayed(this, Constants.HANDLER_DELAY);
            }
        }, Constants.HANDLER_DELAY);
    }

    private void waitForData() {

        // Join threads
        try {
            getSongs.join();
            getPlaylists.join();
            getAlbums.join();
            getArtists.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initMainView() {

        // Init views
        viewPager = (ViewPager) findViewById(R.id.headers);
        PagerAdapter pagerAdapter = new SlidePagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(pagerAdapter);

        // Give tab layout
        FontTabLayout tabLayout = (FontTabLayout) findViewById(R.id.slider);
        tabLayout.setupWithViewPager(viewPager);

        // Set title
        MainActivity.this.setTitle("Echo Music");
    }

    public void updateContent() {
        Log.d(TAG, "Updating content.");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewPager.getAdapter().notifyDataSetChanged();
            }
        });
    }

    public void updateAll() {
        Log.d(TAG, "Updating all.");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Get data
                getData();
                waitForData();

                // Update adapter
                viewPager.getAdapter().notifyDataSetChanged();
            }
        });
    }

    public void updateNew(final String path, final String url) {
        Log.d(TAG, "Updating new.");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // Get data
                Log.e(TAG, "Updating on UI thread.");
                getData();
                waitForData();

                // Update individual song
                for (Song song: songList) {
                    if (song.getPath().equals(path)) {
                        Log.e(TAG, "Updating new song metadata: " + song.getName() + " " + Long.toString(song.getAlbumId()));
                        MediaDataUtils.updateSongMetadata(song.getId(), song.getAlbumId(), url, path, activity);
                        break;
                    }
                }

                // Update adapter
                getData();
                waitForData();
                viewPager.getAdapter().notifyDataSetChanged();
            }
        });
    }

    public void choosePhoto(long id) {
        albumId = id;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.RESULT_CHOOSE_ALBUM_COVER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Update main activity request
        if (requestCode == Constants.RESULT_UPDATE_ACTIVITY && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                boolean change = data.getBooleanExtra(Constants.RESULT_DEFAULT, false);
                if (change) {
                    updateAll();
                }
            }
        }

        // Change photo request
        if (requestCode == Constants.RESULT_CHOOSE_ALBUM_COVER && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                String[] file = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(uri, file, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    String path = cursor.getString(cursor.getColumnIndex(file[0]));
                    cursor.close();
                    MediaDataUtils.changeAlbumArt(path, albumId, getApplicationContext());
                    updateAll();
                }
            } else {
                Log.e(TAG, "Error, no data.");
            }
        }
    }

    @Override
    protected void selectNavItem(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.library: {
                break;
            }
            case R.id.queue: {
                Intent intent = new Intent(this, QueueActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.now_playing: {
                Intent intent = new Intent(this, MusicPlayerActivity.class);
                intent.putExtra(Constants.MUSIC_ID, currSong);
                startActivity(intent);
                break;
            }
            case R.id.equalizer: {
                NavUtils.goToEqualizer(activity);
                break;
            }
            case R.id.info: {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Echo Music v1.3");
                builder.setMessage("\nEcho Music is an MP3 player with built-in video-to-mp3 conversion, developed by Echo Labs. Logo icon by icons8.net.\n\n" +
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
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public List<Song> getSongList() {
        return songList;
    }

    public List<Playlist> getPlaylistList() {
        return playlistList;
    }

    public List<Album> getAlbumList() {
        return albumList;
    }

    public List<Artist> getArtistList() {
        return artistList;
    }

    public long getCurrSong() {
        return currSong;
    }

    public void setMusicBound(boolean bound) {
        this.musicBound = bound;
    }

    @Override
    public void onDestroy() {
        PicassoTools.clearCache(Picasso.with(this));
        super.onDestroy();
    }
}
