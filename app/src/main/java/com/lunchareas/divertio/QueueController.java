package com.lunchareas.divertio;

import java.util.List;

public class QueueController {

    private PlaylistData playlistData;
    private List<SongData> songList;

    public QueueController(PlaylistData playlistData) {
        this.playlistData = playlistData;
        this.songList = playlistData.getSongList();
    }

    public void startQueue() {
        for (SongData songData: songList) {
            
        }
    }
}
