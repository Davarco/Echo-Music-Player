package com.lunchareas.echomp.activities;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.lunchareas.echomp.R;
import com.lunchareas.echomp.dataloaders.SongLoader;
import com.lunchareas.echomp.dialogs.PlaylistAddSongsDialog;
import com.lunchareas.echomp.fragments.MusicPlayerFragment;
import com.lunchareas.echomp.models.Song;
import com.lunchareas.echomp.services.MediaService;
import com.lunchareas.echomp.utils.Constants;
import com.lunchareas.echomp.utils.ImageUtils;
import com.lunchareas.echomp.utils.MediaControlUtils;
import com.lunchareas.echomp.utils.MediaDataUtils;
import com.lunchareas.echomp.utils.NavUtils;
import com.lunchareas.echomp.utils.ShareUtils;
import com.ohoussein.playpause.PlayPauseView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MusicPlayerActivity extends BasePlayerActivity {

    private static final String TAG = MusicPlayerActivity.class.getName();
    private final Activity activity = this;

    private PlayPauseView ctrlButton;
    private SeekBar progressBar;
    private GestureDetector gestureDetector;
    private ImageView shuffleButton;
    private ImageView prevButton;
    private ImageView nextButton;
    private ImageView repeatButton;
    private Handler handler;

    private boolean musicBound;
    private boolean isChanging;
    private boolean isShuffled;
    private boolean isRepeating;
    private boolean isPlay;
    private long albumId;
    private int prevIdx;

    public MusicPlayerActivity() {
        super(R.layout.activity_music_player);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MusicPlayerActivity.this.setTitle("");
    }

    @Override
    @SuppressLint("NewApi")
    protected void initMainView() {

        // Get the buttons
        ctrlButton = (PlayPauseView) findViewById(R.id.play_button);
        shuffleButton = (ImageView) findViewById(R.id.shuffle);
        prevButton = (ImageView) findViewById(R.id.prev);
        nextButton = (ImageView) findViewById(R.id.next);
        repeatButton = (ImageView) findViewById(R.id.repeat);
        isShuffled = false;
        isPlay = false;

        // Set listeners
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isShuffled) {
                    shuffleButton.setImageResource(R.drawable.ic_shuffle_red);
                    MediaControlUtils.shuffle(activity);
                    isShuffled = true;
                } else {
                    shuffleButton.setImageResource(R.drawable.ic_shuffle);
                    MediaControlUtils.shuffle(activity);
                    isShuffled = false;
                }
            }
        });
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaControlUtils.prev(activity);
                if (isPlay) {
                    ctrlButton.toggle();
                    isPlay = false;
                }
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaControlUtils.next(activity);
                if (isPlay) {
                    ctrlButton.toggle();
                    isPlay = false;
                }
            }
        });
        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRepeating) {
                    repeatButton.setImageResource(R.drawable.ic_repeat_red);
                    MediaControlUtils.repeat(activity);
                    isRepeating = true;
                } else {
                    repeatButton.setImageResource(R.drawable.ic_repeat);
                    MediaControlUtils.repeat(activity);
                    isRepeating = false;
                }
            }
        });

        // Get the views
        progressBar = (SeekBar) findViewById(R.id.progress_bar);
        progressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        progressBar.getThumb().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        gestureDetector = new GestureDetector(activity, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1.getX() < e2.getX() && Math.abs(e1.getX()-e2.getX()) > 150) {
                    // Swipe right, go to previous
                    MediaControlUtils.prev(activity);
                    return true;
                }

                if (e1.getX() > e2.getX() && Math.abs(e1.getX()-e2.getX()) > 150) {
                    // Swipe left, go to next
                    MediaControlUtils.next(activity);
                    return true;
                }

                return false;
            }
        });

    }

    @Override
    protected void getDispData() {

        // Get song id and list
        songList = MediaService.getSongList();
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey(Constants.MUSIC_ID)) {
                currSong = getIntent().getLongExtra(Constants.MUSIC_ID, 0);
            } else {
                currSong = -1;
            }
        } else {
            currSong = -1;
        }
    }

    @Override
    @SuppressLint("NewApi")
    protected void showDispData() {

        // Extract data onto view
        if (currSong != -1 && songList != null) {
            for (final Song song: songList) {
                if (song.getId() == currSong) {

                    // Add arguments
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.SONG_NAME, song.getName());
                    bundle.putString(Constants.SONG_ARTIST, song.getArtist());
                    bundle.putString(Constants.SONG_ALBUM, song.getAlbum());
                    bundle.putLong(Constants.ALBUM_ID, song.getAlbumId());

                    // Replace fragment
                    Fragment fragment = new MusicPlayerFragment();
                    fragment.setArguments(bundle);
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_content, fragment);
                    fragmentTransaction.commit();

                    // Change background
                    LinearLayout linearLayout = (LinearLayout) findViewById(R.id.background);
                    try {
                        Bitmap bitmap = ImageUtils.blurBitmap(ImageUtils.getResizedBitmap(MediaStore.Images.Media.getBitmap(
                                getContentResolver(), MediaDataUtils.getAlbumArt(song.getAlbumId())), 400, 400), activity);
                        linearLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
                        linearLayout.getBackground().setAlpha(200);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Set prev idx
                    prevIdx = songList.indexOf(song);
                }
            }
        }
    }

    @Override
    @SuppressLint("NewApi")
    protected void updateDispData() {

        // Get song list
        songList = MediaService.getSongList();

        // Get new song and set data
        if (currSong != -1 && songList != null) {

            // Get song
            final Song song = MediaControlUtils.findSongById(currSong);
            if (song != null) {

                // Set arguments
                Bundle bundle = new Bundle();
                bundle.putString(Constants.SONG_NAME, song.getName());
                bundle.putString(Constants.SONG_ARTIST, song.getArtist());
                bundle.putString(Constants.SONG_ALBUM, song.getAlbum());
                bundle.putLong(Constants.ALBUM_ID, song.getAlbumId());

                // Setup fragment
                Fragment fragment = new MusicPlayerFragment();
                fragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

                // Setup animation based on prev idx
                if (prevIdx < songList.indexOf(song))
                    fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
                else
                    fragmentTransaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
                fragmentTransaction.replace(R.id.main_content, fragment);
                fragmentTransaction.commit();

                // Change background
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.background);
                try {
                    Bitmap bitmap = ImageUtils.blurBitmap(ImageUtils.getResizedBitmap(MediaStore.Images.Media.getBitmap(
                            getContentResolver(), MediaDataUtils.getAlbumArt(song.getAlbumId())), 400, 400), activity);
                    linearLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
                    linearLayout.getBackground().setAlpha(200);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Set prev idx
                prevIdx = songList.indexOf(song);
            }
        }
    }

    @Override
    protected void initListener() {

        // Add play button listener
        ctrlButton.toggle(false);
        ctrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicBound) {
                    MediaControlUtils.pause(activity);
                    if (!isPlay) {
                        ctrlButton.toggle();
                        isPlay = true;
                    }
                } else {
                    MediaControlUtils.start(activity);
                    if (isPlay) {
                        ctrlButton.toggle();
                        isPlay = false;
                    }
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
                if (isPlay) {
                    ctrlButton.toggle();
                    isPlay = false;
                }
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
                    musicBound = MediaService.getStatus();

                    // Get if currently shuffled
                    boolean currShuffled = MediaService.getShuffleMode();
                    if (currShuffled != isShuffled) {
                        if (currShuffled) {
                            shuffleButton.setImageResource(R.drawable.ic_shuffle_red);
                            isShuffled = true;
                        } else {
                            shuffleButton.setImageResource(R.drawable.ic_shuffle);
                            isShuffled = false;
                        }
                    }

                    // Get if currently shuffled
                    boolean currRepeated = MediaService.getRepeatMode();
                    if (currRepeated != isRepeating) {
                        if (currRepeated) {
                            repeatButton.setImageResource(R.drawable.ic_repeat_red);
                            isShuffled = true;
                        } else {
                            repeatButton.setImageResource(R.drawable.ic_repeat);
                            isShuffled = false;
                        }
                    }

                    // Update if different song
                    long tempSong = MediaService.getCurrSong();
                    if (tempSong != currSong) {
                        currSong = tempSong;
                        updateDispData();
                    }

                    /*
                    TODO: Fix progress bar glitch on change
                     */
                    // Set location based on position/duration
                    Song song = MediaControlUtils.findSongById(currSong);
                    assert song != null;
                    int songPosition = MediaService.getSongPos();
                    //int songDuration = intent.getIntExtra(Constants.MUSIC_DURATION, 0);
                    int songDuration = song.getDuration();
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

                } catch (Exception ignored) { ignored.printStackTrace(); }

                handler.postDelayed(this, Constants.HANDLER_DELAY);

            }
        }, Constants.HANDLER_DELAY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDispData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow_menu_music_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
            case R.id.share: {
                ShareUtils.shareTrack(activity, currSong);
                return true;
            }
            case R.id.equalizer: {
                NavUtils.goToEqualizer(activity);
                return true;
            }
            case R.id.change_cover: {
                Song song = MediaControlUtils.findSongById(currSong);
                if (song != null)
                    choosePhoto(song.getAlbumId());
                return true;
            }
            case R.id.add_to_playlist: {
                Song temp = MediaControlUtils.findSongById(currSong);
                if (temp != null) {
                    Bundle bundle = new Bundle();
                    ArrayList<Song> tempList = new ArrayList<>();
                    tempList.add(temp);
                    bundle.putParcelableArrayList(Constants.SONG_LIST, tempList);
                    DialogFragment dialogFragment = new PlaylistAddSongsDialog();
                    dialogFragment.setArguments(bundle);
                    dialogFragment.show(activity.getFragmentManager(), "AddToPlaylist");
                }
                return true;
            }
            case R.id.go_to_artist: {
                Song temp = SongLoader.findSongById(currSong);
                if (temp != null)
                    NavUtils.goToArtist(activity, temp.getArtistId());
                return true;
            }
            case R.id.go_to_album: {
                Song temp = SongLoader.findSongById(currSong);
                if (temp != null)
                    NavUtils.goToAlbum(activity, temp.getAlbumId());
                return true;
            }
        }

        return false;
    }

    public void choosePhoto(long id) {
        albumId = id;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.RESULT_CHOOSE_ALBUM_COVER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Change photo request
        if (resultCode == RESULT_OK && requestCode == Constants.RESULT_CHOOSE_ALBUM_COVER) {
            if (data != null) {
                Uri uri = data.getData();
                String[] file = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(uri, file, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    String path = cursor.getString(cursor.getColumnIndex(file[0]));
                    cursor.close();
                    MediaDataUtils.changeAlbumArt(path, albumId, getApplicationContext());
                    updateDispData();
                }
            } else {
                Log.e(TAG, "Error, no data.");
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void onDestroy() {
        PicassoTools.clearCache(Picasso.with(this));
        super.onDestroy();
    }
}
