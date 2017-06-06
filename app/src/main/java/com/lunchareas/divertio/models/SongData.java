package com.lunchareas.divertio.models;

import android.graphics.drawable.Drawable;

public class SongData {

    private String songName;
    private String songPath;
    private String songArtist;
    private Drawable songCover;

    public SongData(String name, String path, String artist, Drawable icon) {
        this.songName = name;
        this.songPath = path;
        this.songArtist = artist;
        this.songCover = icon;
    }

    public SongData(String name, String path, String artist) {
        this.songName = name;
        this.songPath = path;
        this.songArtist = artist;
    }

    public SongData(String name, String path) {
        this.songName = name;
        this.songPath = path;
    }

    public String getSongName() {
        return this.songName;
    }

    public String getSongPath() {
        return this.songPath;
    }

    public String getSongArtist() {
        return this.songArtist;
    }

    public Drawable getSongCover() {
        return this.songCover;
    }

    public void setSongName(String name) {
        this.songName = name;
    }

    public void setSongPath(String path) {
        this.songPath = path;
    }

    public void setSongArtist(String artist) {
        this.songArtist = artist;
    }

    public void setSongCover(Drawable icon) {
        this.songCover = icon;
    }

    @Override
    public boolean equals(Object other) {
        SongData otherSong = (SongData)other;
        if (otherSong.getSongName().equals(this.getSongName()) || otherSong.getSongPath().equals(this.getSongPath())) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return songName + "\n" + songPath + "\n" + songArtist + "\n";
    }
}
