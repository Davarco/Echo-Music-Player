package com.lunchareas.divertio;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class SongData {

    private String songName;
    private String songPath;
    private String songArtist;
    private int songDuration;
    private Drawable songIcon;

    SongData(String name, String path, String artist, int duration, Drawable icon) {
        this.songName = name;
        this.songPath = path;
        this.songArtist = artist;
        this.songDuration = duration;
        this.songIcon = icon;
    }

    SongData(String name, String path) {
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

    public int getSongDuration() {
        return this.songDuration;
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

    public void setSongDuration(int duration) {
        this.songDuration = duration;
    }

    public void setSongIcon(Drawable icon) {
        this.songIcon = icon;
    }
}
