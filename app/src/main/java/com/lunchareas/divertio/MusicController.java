package com.lunchareas.divertio;

import java.util.List;

public interface MusicController {

    void sendPlaylistCreateIntent(List<SongData> songList);
    void sendMusicCreateIntent(String path);
    void sendMusicStartIntent();
    void sendMusicPauseIntent();
    void sendMusicChangeIntent(int position);
}
