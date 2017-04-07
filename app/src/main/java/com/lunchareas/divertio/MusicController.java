package com.lunchareas.divertio;

public interface MusicController {

    void sendMusicCreateIntent(String path);
    void sendMusicStartIntent();
    void sendMusicPauseIntent();
    void sendMusicChangeIntent(int position);
}
