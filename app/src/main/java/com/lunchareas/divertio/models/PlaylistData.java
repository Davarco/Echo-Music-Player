package com.lunchareas.divertio.models;


import android.graphics.drawable.Drawable;

import java.util.List;
import java.util.Objects;

public class PlaylistData {

    private String playlistName;
    private Drawable playlistIcon;
    private List<SongData> songList;
    private int numSongs;

    public PlaylistData(String name, Drawable icon, List<SongData> list, int num) {
        this.playlistName = name;
        this.playlistIcon = icon;
        this.songList = list;
        this.numSongs = num;
    }

    public PlaylistData(String name, List<SongData> list) {
        this.playlistName = name;
        this.songList = list;
        this.numSongs = list.size();
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

    @Override
    public boolean equals(Object other) {
        PlaylistData otherPlaylist = (PlaylistData) other;
        if (otherPlaylist.getPlaylistName().equals(this.getPlaylistName())) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.playlistName + "\n");
        for (int i = 0; i < songList.size(); i++) {
            stringBuilder.append("Song " + Integer.toString(i+1) + ": " + songList.get(i).getSongName() + "\n");
        }
        stringBuilder.append("Number of Songs: " + numSongs);
        return stringBuilder.toString();
    }
}
