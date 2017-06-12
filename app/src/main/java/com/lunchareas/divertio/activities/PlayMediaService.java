package com.lunchareas.divertio.activities;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.models.SongDBHandler;
import com.lunchareas.divertio.models.SongData;

import java.util.List;

public class PlayMediaService extends Service {

    private static final String TAG = PlayMediaService.class.getName();

    public static final String MUSIC_RESULT = "REQUEST_PROCESSED";
    public static final String MUSIC_POSITION = "POSITION";
    public static final String MUSIC_DURATION = "DURATION";
    public static final String MUSIC_CURR = "CURR_SONG";

    public static final String MUSIC_CREATE = "CREATE";
    public static final String MUSIC_CHANGE = "CHANGE";
    public static final String MUSIC_PLAY = "PLAY";
    public static final String MUSIC_PAUSE = "PAUSE";
    public static final String MUSIC_STOP = "STOP";
    public static final String MUSIC_PREV = "PREV";
    public static final String MUSIC_NEXT = "NEXT";
    public static final String MUSIC_STATUS = "STATUS";
    public static final String PLAYLIST_CREATE = "PLAYLIST_CREATE";

    private MediaPlayer mp = null;
    private int idx;

    // Set up broadcaster to activity to update progress bar
    private LocalBroadcastManager musicUpdater;
    private Thread musicUpdaterThread;
    private boolean musicReset;
    private boolean inPlaylistMode;
    private String currSong;
    private SongData songData;
    private String[] songNameList;

    // Media session stuff
    private MediaSession mediaSession;

    @Override
    public int onStartCommand(Intent workIntent, int flags, int startId) {

        // Get cmd
        String action = workIntent.getAction();

        // Start media session if needed
        if (action != null) {
            Log.e(TAG, action);
            if (action.equals(MUSIC_CREATE)) {
                initBroadcaster();
                initMedia(workIntent.getExtras().getString(PlayMediaService.MUSIC_CREATE));
            }

            // Start handler
            handleIntent(workIntent, action);
        }

        return START_STICKY;
    }

    private void initBroadcaster() {

        // Create the thread to update progress bar
        musicUpdater = LocalBroadcastManager.getInstance(this);
        musicUpdaterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int songPosition;
                int songDuration;
                do {

                    // Wait for reset to finish if necessary
                    while (musicReset) {
                        try {
                            Thread.sleep(200);
                            Log.d(TAG, "Pausing music updater!");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // Change duration if track changes
                    songPosition = mp.getCurrentPosition();
                    songDuration = mp.getDuration();
                    //Log.d(TAG, "Song service position: " + songPosition + "\nSong service duration: " + songDuration);

                    // Create and send intent with position and duration
                    Intent songIntent = new Intent(MUSIC_RESULT);
                    songIntent.putExtra(MUSIC_POSITION, songPosition);
                    songIntent.putExtra(MUSIC_DURATION, songDuration);
                    songIntent.putExtra(MUSIC_CURR, currSong);
                    songIntent.putExtra(MUSIC_STATUS, mp.isPlaying());
                    musicUpdater.sendBroadcast(songIntent);

                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } while (mp != null && mp.getCurrentPosition() <= songDuration);
            }
        });
    }

    @SuppressLint("NewApi")
    private void initMedia(String songName) {

        // Get song data
        currSong = songName;
        songData = new SongDBHandler(this).getSongData(currSong);

        // Pause song if playing
        if (mp != null && mp.isPlaying()) {
            mp.pause();
        }
        initMusicPlayer();

        // Media session init
        mediaSession = new MediaSession(this, "MusicService");
        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                mp.start();
                musicUpdaterThread.start();
                buildNotification(MUSIC_PAUSE);
            }

            @Override
            public void onPause() {
                if (mp.isPlaying()) {
                    mp.pause();
                    buildNotification(MUSIC_PLAY);
                } else {
                    Log.e(TAG, "Tried to pause, MP is not playing!");
                    // onPlay();
                }
            }

            @Override
            public void onStop() {
                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(1);
                Intent intent = new Intent(getApplicationContext(), PlayMediaService.class);
                stopService(intent);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                if (!inPlaylistMode) {
                    List<SongData> songList = new SongDBHandler(getApplicationContext()).getSongDataList();
                    int pos = songList.indexOf(songData);
                    pos += 1;
                    if (pos >= songList.size()) {
                        pos = 0;
                    }
                    initMedia(songList.get(pos).getSongName());
                    buildNotification(MUSIC_PAUSE);
                } else {
                    musicReset = true;
                    idx += 1;
                    if (idx < songNameList.length) {
                        initMedia(songNameList[idx]);
                    } else {
                        idx -= 1;
                    }
                    musicReset = false;
                    buildNotification(MUSIC_PAUSE);
                }
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                if (!inPlaylistMode) {
                    List<SongData> songList = new SongDBHandler(getApplicationContext()).getSongDataList();
                    int pos = songList.indexOf(songData);
                    pos -= 1;
                    if (pos < 0) {
                        pos = songList.size() - 1;
                    }
                    initMedia(songList.get(pos).getSongName());
                    buildNotification(MUSIC_PAUSE);
                } else {
                    musicReset = true;
                    idx -= 1;
                    if (idx >= 0) {
                        initMedia(songNameList[idx]);
                    } else {
                        idx += 1;
                    }
                    musicReset = false;
                    buildNotification(MUSIC_PAUSE);
                }
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                mp.seekTo((int)pos);
            }
        });
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Start song
        mediaSession.getController().getTransportControls().play();
    }

    @SuppressLint("NewApi")
    private void handleIntent(Intent intent, String action) {

        // Handle different events
        if (action.equals(PlayMediaService.MUSIC_PLAY) && mp != null) {
            //Log.e(TAG, "Trying to play!");
            mediaSession.getController().getTransportControls().play();
        } else if (action.equals(PlayMediaService.MUSIC_PAUSE) && mp != null) {
            //Log.e(TAG, "Trying to pause!");
            mediaSession.getController().getTransportControls().pause();
        } else if (action.equals(PlayMediaService.MUSIC_CHANGE) && mp != null) {
            int position = intent.getExtras().getInt(MUSIC_CHANGE);
            mediaSession.getController().getTransportControls().seekTo(position);
        } else if (action.equals(PlayMediaService.MUSIC_PREV)) {
            mediaSession.getController().getTransportControls().skipToPrevious();
        } else if (action.equals(PlayMediaService.MUSIC_NEXT)) {
            mediaSession.getController().getTransportControls().skipToNext();
        } else if (action.equals(PlayMediaService.PLAYLIST_CREATE)) {
            String[] songPathList = intent.getExtras().getStringArray(PlayMediaService.PLAYLIST_CREATE);
            beginPlaylistQueue(songPathList);
        } else if (action.equals(PlayMediaService.MUSIC_CREATE)) {

        } else {
            //Log.e(TAG, "Command sent to PlayMediaService not found.");
            //System.out.println(intentCmd);
            Log.e(TAG, "No command sent, bundle empty.");
        }
    }

    @SuppressLint("NewApi")
    private void buildNotification(String cmd) {

        // Set style
        Notification.MediaStyle style = new Notification.MediaStyle();
        style.setMediaSession(mediaSession.getSessionToken());

        // Setup intent and notification
        Intent intent = new Intent(getApplicationContext(), PlayMediaService.class);
        intent.putExtra(MUSIC_STOP, 0);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_headphone)
                .setContentTitle(songData.getSongName())
                .setContentText(songData.getSongArtist())
                .setDeleteIntent(pendingIntent)
                .setStyle(style);

        // Add actions
        builder.addAction(createAction(R.drawable.ic_prev_noti, "Previous", MUSIC_PREV));
        if (cmd.equals(MUSIC_PLAY)) {
            builder.addAction(createAction(R.drawable.ic_play_noti, "Play", MUSIC_PLAY));
        }
        if (cmd.equals(MUSIC_PAUSE)) {
            builder.addAction(createAction(R.drawable.ic_pause_noti, "Pause", MUSIC_PAUSE));
        }
        builder.addAction(createAction(R.drawable.ic_next_noti, "Next", MUSIC_NEXT));

        // Notify
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }

    private void beginPlaylistQueue(String[] songList) {

        // Set to playlist mode
        this.songNameList = songList;
        inPlaylistMode = true;

        // Play the first song
        initMedia(songNameList[0]);
        initBroadcaster();

        // Setup for completion
        idx = 0;
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                idx += 1;
                if (idx < songNameList.length) {
                    try {

                        // Set new song
                        musicReset = true;
                        initMedia(songNameList[idx]);
                        musicReset = false;

                        //musicUpdaterThread.start();
                        Log.d(TAG, "Playing next song, number " + Integer.toString(idx));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "Finished playlist.");
                    inPlaylistMode = false;
                }
            }
        });
    }

    private void initMusicPlayer() {
        mp = MediaPlayer.create(this, Uri.parse(songData.getSongPath()));
        if (mp != null) {
            mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }

    @SuppressLint("NewApi")
    private Notification.Action createAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), PlayMediaService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new Notification.Action.Builder(icon, title, pendingIntent).build();
    }

    @Override
    public IBinder onBind(Intent i) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "PlayMediaService destroyed...");
        mp.release();
        mp = null;
        musicUpdaterThread.interrupt();
    }
}