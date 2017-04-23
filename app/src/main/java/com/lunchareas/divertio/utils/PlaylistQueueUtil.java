package com.lunchareas.divertio.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.lunchareas.divertio.activities.PlayMusicService;
import com.lunchareas.divertio.interfaces.MusicConductor;
import com.lunchareas.divertio.models.PlaylistData;
import com.lunchareas.divertio.models.SongData;

import java.util.Collections;
import java.util.List;

public class PlaylistQueueUtil implements MusicConductor {

    /*
    TODO: Need to test if playlist manager is actually going through all the songs
     */

    private static final String TAG = PlaylistQueueUtil.class.getName();

    private int idx;
    private int firstPos;
    private Context context;
    private List<SongData> songList;

    public PlaylistQueueUtil(int firstPos, PlaylistData playlistData, Context context) {
        this.idx = 0;
        this.firstPos = firstPos;
        this.context = context;
        this.songList = playlistData.getSongList();
    }

    public void startQueue() {
        Log.d(TAG, "Starting queue!");
        sendMusicPauseIntent();
        sendPlaylistCreateIntent(songList);
    }

    @Override
    public void sendPlaylistCreateIntent(List<SongData> songList) {
        Log.d(TAG, "Trying to send playlist create intent.");
        Intent playlistCreateIntent = new Intent(context, PlayMusicService.class);

        // Shuffle list, get first song
        SongData firstSong = songList.get(firstPos);
        Collections.shuffle(songList);

        // Switch 0th with supposed first song
        for (int i = 0; i < songList.size(); i++) {
            if (songList.get(i).getSongName().equals(firstSong.getSongName())) {
                Log.d(TAG, "Swapping " + i + " and " + 0 + ".");
                Collections.swap(songList, i, 0);
                break;
            } else {
                Log.d(TAG, "No swap.");
            }
        }

        // Create the string array
        String[] songPathList = new String[songList.size()];
        for (int i = 0; i < songList.size(); i++) {
            songPathList[i] = songList.get(i).getSongPath();
            Log.d(TAG, "Song " + Integer.toString(i+1) + ": " + songPathList[i]);
        }

        // Send the intent
        playlistCreateIntent.putExtra(PlayMusicService.PLAYLIST_CREATE, songPathList);
        context.startService(playlistCreateIntent);
    }

    @Override
    public void sendMusicCreateIntent(String path) {
        Log.d(TAG, "Trying to send music create intent.");
        Intent musicCreateIntent = new Intent(context, PlayMusicService.class);
        Log.d(TAG, "Passing string to create intent: " + path);
        musicCreateIntent.putExtra(PlayMusicService.MUSIC_CREATE, path);
        context.startService(musicCreateIntent);
    }

    @Override
    public void sendMusicStartIntent() {
        Intent musicStartIntent = new Intent(context, PlayMusicService.class);
        musicStartIntent.putExtra(PlayMusicService.MUSIC_START, 0);
        context.startService(musicStartIntent);
    }

    @Override
    public void sendMusicPauseIntent() {
        Intent musicPauseIntent = new Intent(context, PlayMusicService.class);
        musicPauseIntent.putExtra(PlayMusicService.MUSIC_PAUSE, 0);
        context.startService(musicPauseIntent);
    }

    @Override
    public void sendMusicChangeIntent(int position) {
        Intent musicChangeIntent = new Intent(context, PlayMusicService.class);
        musicChangeIntent.putExtra(PlayMusicService.MUSIC_CHANGE, position);
        context.startService(musicChangeIntent);
    }
}
