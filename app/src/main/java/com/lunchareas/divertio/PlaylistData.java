package com.lunchareas.divertio;


import android.graphics.drawable.Drawable;

import java.util.List;

public class PlaylistData {

    private String playlistName;
    private Drawable playlistIcon;
    private List<SongData> songList;
    private int numSongs;

    PlaylistData(String name, Drawable icon, List<SongData> list, int num) {
        this.playlistName = name;
        this.playlistIcon = icon;
        this.songList = list;
        this.numSongs = num;
    }

    PlaylistData(String name, List<SongData> list) {
        this.playlistName = name;
        this.songList = list;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public Drawable getPlaylistIcon() {
        return playlistIcon;
    }

    public List<SongData> getSongList() {
        return songList;
    }

    public int getNumSongs() {
        return numSongs;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public void setPlaylistIcon(Drawable playlistIcon) {
        this.playlistIcon = playlistIcon;
    }

    public void setSongList(List<SongData> songList) {
        this.songList = songList;
    }

    public void setNumSongs(int numSongs) {
        this.numSongs = numSongs;
    }
}
