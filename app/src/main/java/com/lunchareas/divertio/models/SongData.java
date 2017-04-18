package com.lunchareas.divertio.models;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class SongData {

    private String songName;
    private String songPath;
    private String songArtist;
    private Drawable songIcon;

    public SongData(String name, String path, String artist, Drawable icon) {
        this.songName = name;
        this.songPath = path;
        this.songArtist = artist;
        this.songIcon = icon;
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

    public Drawable getSongIcon() {
        return this.songIcon;
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

    public void setSongIcon(Drawable icon) {
        this.songIcon = icon;
    }

    @Override
    public boolean equals(Object other) {
        SongData otherSong = (SongData)other;
        if (otherSong.getSongName().equals(this.getSongName())) {
            return true;
        }

        return false;
    }
}
