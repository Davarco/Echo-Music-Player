package com.lunchareas.divertio.interfaces;

import com.lunchareas.divertio.models.SongData;

import java.util.List;

public interface MusicConductor {

    void sendPlaylistCreateIntent(List<SongData> songList);
    void sendMusicCreateIntent(String path);
    void sendMusicStartIntent();
    void sendMusicPauseIntent();
    void sendMusicChangeIntent(int position);
}
