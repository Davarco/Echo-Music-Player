package com.lunchareas.divertio;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Collections;
import java.util.List;

public class PlaylistController implements MusicController {

    private int idx;
    private Context context;
    private List<SongData> songList;

    public PlaylistController(PlaylistData playlistData, Context context) {
        this.idx = 0;
        this.context = context;
        this.songList = playlistData.getSongList();
    }

    public void startQueue() {
        System.out.println("Starting queue!");
        sendMusicPauseIntent();
        sendPlaylistCreateIntent(songList);
    }

    @Override
    public void sendPlaylistCreateIntent(List<SongData> songList) {
        System.out.println("Trying to send playlist create intent.");
        Intent playlistCreateIntent = new Intent(context, PlayMusicService.class);

        Collections.shuffle(songList);

        // Create the string array
        String[] songPathList = new String[songList.size()];
        for (int i = 0; i < songList.size(); i++) {
            songPathList[i] = songList.get(i).getSongPath();
            System.out.println("Song " + Integer.toString(i+1) + ": " + songPathList[i]);
        }

        // send the intent
        playlistCreateIntent.putExtra(PlayMusicService.PLAYLIST_CREATE, songPathList);
        context.startService(playlistCreateIntent);
    }

    @Override
    public void sendMusicCreateIntent(String path) {
        System.out.println("Trying to send music create intent.");
        Intent musicCreateIntent = new Intent(context, PlayMusicService.class);
        System.out.println("Passing string to create intent: " + path);
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
