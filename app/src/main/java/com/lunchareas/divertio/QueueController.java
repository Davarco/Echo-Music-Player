package com.lunchareas.divertio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

public class QueueController implements MusicController {

    private PlaylistData playlistData;
    private List<SongData> songList;
    private BroadcastReceiver finishBroadcastReceiver;

    public QueueController(PlaylistData playlistData) {
        this.playlistData = playlistData;
        this.songList = playlistData.getSongList();

        // Create broadcast listener for music finishes
        finishBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };
    }

    public void startQueue() {

    }
}
