//package com.lunchareas.echomp.services;
//
//
//import android.content.BroadcastReceiver;
//import android.content.IntentFilter;
//import android.os.Build;
//import android.support.v7.*;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.media.AudioManager;
//import android.media.MediaPlayer;
//import android.support.v4.media.session.MediaSessionCompat;
//import android.net.Uri;
//import android.os.IBinder;
//import android.os.PowerManager;
//import android.util.Log;
//
//import com.lunchareas.echomp.R;
//import com.lunchareas.echomp.databases.RecentlyPlayedStore;
//import com.lunchareas.echomp.databases.TopTracksStore;
//import com.lunchareas.echomp.models.Song;
//import com.lunchareas.echomp.utils.Constants;
//import com.lunchareas.echomp.utils.MediaControlUtils;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//public class BackupService extends Service {
//
//    private static final String TAG = MediaService.class.getName();
//
//    private static MediaPlayer mp;
//    private static MediaSessionCompat mediaSession;
//    private static List<Song> songList;
//    private static List<Boolean> songPlayed;
//    private static Song song;
//
//    private static boolean inShuffleMode;
//    private static boolean inRepeatMode;
//    private static long currSong;
//    private static int idx;
//    private static int audioId;
//    private boolean calling;
//
//    @Override
//    public int onStartCommand(Intent workIntent, int flags, int startId) {
//
//        try {
//
//            // Get command
//            String action = null;
//            if (workIntent != null) {
//                action = workIntent.getAction();
//            }
//
//            // Start media session if needed
//            if (action != null) {
//
//                // Start handler
//                handleIntent(workIntent, action);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return START_STICKY;
//    }
//
//
//    private void initMedia(int pos) {
//
//        // Get song data
//        song = songList.get(pos);
//        currSong = song.getId();
//
//        // Update top tracks
//        TopTracksStore topTracksStore = new TopTracksStore(getApplicationContext());
//        topTracksStore.incSong(song.getId());
//
//        // Update recently played
//        RecentlyPlayedStore recentlyPlayedStore = new RecentlyPlayedStore(getApplicationContext());
//        recentlyPlayedStore.insert(song.getId());
//
//        // Media session init
//        mediaSession = new MediaSessionCompat(this, "MusicService");
//        mediaSession.setCallback(new MediaSessionCompat.Callback() {
//            @Override
//            public void onPlay() {
//                try {
//                    mp.start();
//                    songPlayed.set(idx, true);
//                    buildNotification(Constants.MUSIC_PAUSE);
//                } catch (Exception ignored) {}
//            }
//
//            @Override
//            public void onPause() {
//                try {
//                    if (mp.isPlaying()) {
//                        mp.pause();
//                        buildNotification(Constants.MUSIC_PLAY);
//                    } else {
//                        Log.e(TAG, "Tried to pause, MP is not playing!");
//                    }
//                } catch (Exception ignored) {}
//            }
//
//            @Override
//            public void onStop() {
//                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//                notificationManager.cancel(1);
//                Intent intent = new Intent(getApplicationContext(), MediaService.class);
//                stopService(intent);
//            }
//
//            @Override
//            public void onSkipToNext() {
//                try {
//                    super.onSkipToNext();
//                    nextSong();
//                    buildNotification(Constants.MUSIC_PAUSE);
//                } catch (Exception ignored) {}
//            }
//
//            @Override
//            public void onSkipToPrevious() {
//                try {
//                    super.onSkipToPrevious();
//                    idx -= 1;
//                    if (idx >= 0) {
//                        beginSongList();
//                    } else {
//                        idx = songPlayed.size()-1;
//                        beginSongList();
//                    }
//                    buildNotification(Constants.MUSIC_PAUSE);
//                } catch (Exception ignored) {}
//            }
//
//            @Override
//            public void onSeekTo(long pos) {
//                try {
//                    //super.onSeekTo(pos);
//                    mp.seekTo((int) pos);
//                } catch (Exception ignored) {
//                    ignored.printStackTrace();
//                }
//            }
//        });
//        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
//
//        // Pause song if playing
//        if (mp != null && mp.isPlaying()) {
//            mp.pause();
//        }
//        initMusicPlayer();
//    }
//
//    private void handleIntent(Intent intent, String action) {
//
//        // Handle different events
//        try {
//            if (action.equals(Constants.MUSIC_INIT)) {
//                audioId = intent.getExtras().getInt(Constants.MUSIC_ID);
//                registerNoiseReceiver();
//            }
//            if (action.equals(Constants.MUSIC_CREATE)) {
//                if (mp != null) {
//                    mp.pause();
//                    mp.release();
//                    mp = null;
//                }
//                songList = intent.getExtras().getParcelableArrayList(Constants.MUSIC_LIST);
//                assert songList != null;
//                songPlayed = new ArrayList<>(Collections.nCopies(songList.size(), false));
//                // songPlayed = new boolean[songList.size()];
//                idx = intent.getExtras().getInt(Constants.MUSIC_LIST_POS);
//                beginSongList();
//            }
//            if (action.equals(Constants.MUSIC_CREATE_REPEATING)) {
//                if (mp != null) {
//                    mp.pause();
//                    mp.release();
//                    mp = null;
//                }
//                songList = intent.getExtras().getParcelableArrayList(Constants.MUSIC_LIST);
//                assert songList != null;
//                songPlayed = new ArrayList<>(Collections.nCopies(songList.size(), false));
//                // songPlayed = new boolean[songList.size()];
//                idx = intent.getExtras().getInt(Constants.MUSIC_LIST_POS);
//                inRepeatMode = true;
//                beginSongList();
//            }
//            if (action.equals(Constants.MUSIC_DEFAULT)) {
//                if (mp != null) {
//                    mp.pause();
//                    mp.release();
//                    mp = null;
//                }
//                songList = intent.getExtras().getParcelableArrayList(Constants.MUSIC_DEFAULT);
//                assert songList != null;
//                songPlayed = new ArrayList<>(Collections.nCopies(songList.size(), false));
//                // songPlayed = new boolean[songList.size()];
//                idx = 0;
//                beginSongList();
//            }
//            if (action.equals(Constants.MUSIC_DEFAULT_REPEATING)) {
//                if (mp != null) {
//                    mp.pause();
//                    mp.release();
//                    mp = null;
//                }
//                songList = intent.getExtras().getParcelableArrayList(Constants.MUSIC_DEFAULT_REPEATING);
//                assert songList != null;
//                songPlayed = new ArrayList<>(Collections.nCopies(songList.size(), false));
//                // songPlayed = new boolean[songList.size()];
//                idx = 0;
//                inRepeatMode = true;
//                beginSongList();
//            }
//            if (action.equals(Constants.MUSIC_DEFAULT_REPEATING_SPECIFIC)) {
//                if (mp != null) {
//                    mp.pause();
//                    mp.release();
//                    mp = null;
//                }
//                songList = intent.getExtras().getParcelableArrayList(Constants.MUSIC_DEFAULT_REPEATING);
//                int position = intent.getExtras().getInt(Constants.MUSIC_DEFAULT_REPEATING_SPECIFIC);
//                assert songList != null;
//                songPlayed = new ArrayList<>(Collections.nCopies(songList.size(), false));
//                // songPlayed = new boolean[songList.size()];
//                idx = position;
//                inRepeatMode = true;
//                beginSongList();
//            }
//            if (action.equals(Constants.MUSIC_DEFAULT_SHUFFLED)) {
//                if (mp != null) {
//                    mp.pause();
//                    mp.release();
//                    mp = null;
//                }
//                songList = intent.getExtras().getParcelableArrayList(Constants.MUSIC_DEFAULT_SHUFFLED);
//                assert songList != null;
//                songPlayed = new ArrayList<>(Collections.nCopies(songList.size(), false));
//                // songPlayed = new boolean[songList.size()];
//                idx = (int)(Math.random()*songList.size());
//                inShuffleMode = true;
//                beginSongList();
//            }
//            if (action.equals(Constants.MUSIC_DEFAULT_REPEATING_SHUFFLED)) {
//                if (mp != null) {
//                    mp.pause();
//                    mp.release();
//                    mp = null;
//                }
//                songList = intent.getExtras().getParcelableArrayList(Constants.MUSIC_DEFAULT_REPEATING_SHUFFLED);
//                assert songList != null;
//                songPlayed = new ArrayList<>(Collections.nCopies(songList.size(), false));
//                // songPlayed = new boolean[songList.size()];
//                idx = (int)(Math.random()*songList.size());
//                inRepeatMode = true;
//                inShuffleMode = true;
//                beginSongList();
//            }
//            if (action.equals(Constants.MUSIC_ADD_TO_QUEUE)) {
//                List<Song> tempList = intent.getExtras().getParcelableArrayList(Constants.MUSIC_ADD_TO_QUEUE);
//                assert tempList != null;
//                boolean restart = false;
//                if (songList == null) {
//                    restart = true;
//                    songList = new ArrayList<>();
//                }
//                if (songPlayed == null) {
//                    restart = true;
//                    songPlayed = new ArrayList<>();
//                }
//                songList.addAll(tempList);
//                songPlayed.addAll(Collections.nCopies(tempList.size(), false));
//                if (restart)
//                    beginSongList();
//            }
//            if (action.equals(Constants.MUSIC_REMOVE_FROM_QUEUE)) {
//                List<Song> tempList = intent.getExtras().getParcelableArrayList(Constants.MUSIC_REMOVE_FROM_QUEUE);
//                assert tempList != null;
//                for (Song song: tempList) {
//                    for (int i = 0; i < songList.size(); i++) {
//                        if (song.equals(songList.get(i))) {
//                            songList.remove(i);
//                            songPlayed.remove(i);
//                        }
//                    }
//                }
//            }
//            if (action.equals(Constants.MUSIC_PLAY) && mp != null) {
//                mediaSession.getController().getTransportControls().play();
//            }
//            if (action.equals(Constants.MUSIC_PAUSE) && mp != null) {
//                mediaSession.getController().getTransportControls().pause();
//            }
//            if (action.equals(Constants.MUSIC_CHANGE) && mp != null) {
//                int position = intent.getExtras().getInt(Constants.MUSIC_CHANGE);
//                mediaSession.getController().getTransportControls().seekTo(position);
//            }
//            if (action.equals(Constants.MUSIC_PREV)) {
//                mediaSession.getController().getTransportControls().skipToPrevious();
//            }
//            if (action.equals(Constants.MUSIC_NEXT)) {
//                mediaSession.getController().getTransportControls().skipToNext();
//            }
//            if (action.equals(Constants.MUSIC_SHUFFLE)) {
//                inShuffleMode = !inShuffleMode;
//            }
//            if (action.equals(Constants.MUSIC_REPEAT)) {
//                inRepeatMode = !inRepeatMode;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void buildNotification(String cmd) {
//
//        // Set style
//        NotificationCompat.Style style = new NotificationCompat.MediaStyle();
//
//        // Setup intent and notification
//        Intent intent = new Intent(getApplicationContext(), MediaService.class);
//        intent.putExtra(Constants.MUSIC_STOP, 0);
//        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
//        NotificationCompat.Builder builder = (android.support.v7.app.NotificationCompat.Builder) new NotificationCompat.Builder(this)
//                .setSmallIcon(R.drawable.ic_headphone)
//                .setContentTitle(song.getName())
//                .setContentText(song.getArtist())
//                .setDeleteIntent(pendingIntent)
//                .setStyle(style);
//
//        // Add actions
//        builder.addAction(createAction(R.drawable.ic_prev_noti, "Previous", Constants.MUSIC_PREV));
//        if (cmd.equals(Constants.MUSIC_PLAY)) {
//            builder.addAction(createAction(R.drawable.ic_play_noti, "Play", Constants.MUSIC_PLAY));
//        }
//        if (cmd.equals(Constants.MUSIC_PAUSE)) {
//            builder.addAction(createAction(R.drawable.ic_pause_noti, "Pause", Constants.MUSIC_PAUSE));
//        }
//        builder.addAction(createAction(R.drawable.ic_next_noti, "Next", Constants.MUSIC_NEXT));
//
//        // Notify
//        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        manager.notify(1, builder.build());
//    }
//
//    private void beginSongList() {
//
//        // Play the first song
//        initMedia(idx);
//
//        // Setup for completion
//        if (mp != null) {
//            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mediaPlayer) {
//                    nextSong();
//                }
//            });
//        }
//    }
//
//    private void initMusicPlayer() {
//        //mp = MediaPlayer.create(this, Uri.parse(song.getPath()));
//        //mp.stop();
//        if (mp != null) {
//            mp.stop();
//            mp.reset();
//        }
//        try {
//            if (Build.VERSION.SDK_INT < 21)
//                mp = MediaPlayer.create(this, Uri.parse(song.getPath()));
//            else
//                mp = MediaPlayer.create(this, Uri.parse(song.getPath()), null, null, audioId);
//            mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
//            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            mediaSession.getController().getTransportControls().play();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private NotificationCompat.Action createAction(int icon, String title, String intentAction) {
//        Intent intent = new Intent(getApplicationContext(), MediaService.class);
//        intent.setAction(intentAction);
//        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
//        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
//    }
//
//    private void nextSong() {
//
//        if (!inShuffleMode) {
//
//            // Go to next song
//            idx += 1;
//            if (idx < songList.size()) {
//                try {
//
//                    // Set new song
//                    beginSongList();
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            } else {
//                if (inRepeatMode) {
//                    idx = 0;
//                    beginSongList();
//                } else {
//                    idx -= 1;
//                    Log.e(TAG, "Non-shuffled queue completed.");
//                }
//            }
//        } else {
//
//            // Pick random song that hasn't been played
//            List<Integer> temp = new ArrayList<>();
//            for (int i = 0; i < songPlayed.size(); i++) {
//                if (!songPlayed.get(i)) {
//                    temp.add(i);
//                }
//            }
//            if (temp.size() > 0) {
//                int i = (int)(Math.random()*temp.size());
//                idx = temp.get(i);
//                beginSongList();
//            } else {
//                if (inRepeatMode) {
//
//                    // Reset played songs
//                    for (int i = 0; i < songPlayed.size(); i++) {
//                        songPlayed.set(i, false);
//                    }
//
//                    // Find a new idx to start with
//                    List<Integer> indexes = new ArrayList<>();
//                    for (int i = 0; i < songPlayed.size(); i++) {
//                        if (!songPlayed.get(i)) {
//                            indexes.add(i);
//                        }
//                    }
//                    int i = (int)(Math.random()*indexes.size());
//                    idx = indexes.get(i);
//                    beginSongList();
//                } else {
//                    Log.e(TAG, "Shuffled queue completed.");
//                }
//            }
//        }
//    }
//
//    /*
//    Manages audio change! (e.g. unplugged headphones)
//     */
//    private BroadcastReceiver noiseReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.d(TAG, "Detected audio change, pausing.");
//            MediaControlUtils.pause(context);
//        }
//    };
//
//    private void registerNoiseReceiver() {
//
//        // Manage focus changes
//        IntentFilter filter1 = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
//        IntentFilter filter2 = new IntentFilter(AudioManager.ACTION_HEADSET_PLUG);
//        IntentFilter filter3 = new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
//        IntentFilter filter4 = new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED);
//        registerReceiver(noiseReceiver, filter1);
//        registerReceiver(noiseReceiver, filter2);
//        registerReceiver(noiseReceiver, filter3);
//        registerReceiver(noiseReceiver, filter4);
//    }
//
//    public synchronized static int getSongPos() {
//        try {
//            return mp.getCurrentPosition();
//        } catch (Exception ignored) {}
//
//        return Constants.MEDIA_ERROR;
//    }
//
//    public synchronized static int getSongDur() {
//        try {
//            return mp.getDuration();
//        } catch (Exception ignored) {}
//
//        return Constants.MEDIA_ERROR;
//    }
//
//    public synchronized static boolean getStatus() {
//        try {
//            return mp.isPlaying();
//        } catch (Exception ignored) {}
//
//        return true;
//    }
//
//    public synchronized static long getCurrSong() {
//        return currSong;
//    }
//
//    public synchronized static boolean getShuffleMode() {
//        return inShuffleMode;
//    }
//
//    public synchronized static boolean getRepeatMode() {
//        return inRepeatMode;
//    }
//
//    public synchronized static List<Song> getSongList() {
//        return songList;
//    }
//
//    public static void setSongList(List<Song> songs) {
//        songList = songs;
//    }
//
//    public static void setIdx(int i) {
//        idx = i;
//    }
//
//    @Override
//    public IBinder onBind(Intent i) {
//        return null;
//    }
//
//    @Override
//    public void onDestroy() {
//        mp.release();
//        mp = null;
//    }
//}