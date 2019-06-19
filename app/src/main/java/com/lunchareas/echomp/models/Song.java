package com.lunchareas.echomp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable {

    private long id;
    private String name;
    private String path;
    private String artist;
    private long artistId;
    private String album;
    private long albumId;
    private int duration;
    private long date;

    public Song(long id, String name, String path, String artist, long artistId, String album, long albumId, int duration, long date) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.artist = artist;
        this.artistId = artistId;
        this.album = album;
        this.albumId = albumId;
        this.duration = duration;
        this.date = date;
    }

    public Song(Parcel parcel) {
        this.id = parcel.readLong();
        this.name = parcel.readString();
        this.path = parcel.readString();
        this.artist = parcel.readString();
        this.artistId = parcel.readLong();
        this.album = parcel.readString();
        this.albumId = parcel.readLong();
        this.duration = parcel.readInt();
        this.date = parcel.readLong();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return this.name;
    }

    public String getPath() {
        return this.path;
    }

    public String getArtist() {
        return this.artist;
    }

    public long getArtistId() {
        return this.artistId;
    }

    public String getAlbum() {
        return this.album;
    }

    public long getAlbumId() {
        return this.albumId;
    }

    public int getDuration() {
        return this.duration;
    }

    public long getDate() {
        return this.date;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setArtistId(long id) {
        this.artistId = id;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setAlbumId(long id) {
        this.albumId = id;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object other) {
        Song otherSong = (Song) other;
        return (otherSong.getId() == this.getId());
    }

    @Override
    public String toString() {
        return "Name: " + name + "\n" + "ID: " + Long.toString(id) + "\n" +
                "AID: " + Long.toString(albumId) + "\n" + "Path: " + path + "\n";
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(path);
        dest.writeString(artist);
        dest.writeLong(artistId);
        dest.writeString(album);
        dest.writeLong(albumId);
        dest.writeInt(duration);
        dest.writeLong(date);
    }

    public static final Parcelable.Creator CREATOR =
        new Parcelable.Creator() {

            public Song createFromParcel(Parcel source) {
                return new Song(source);
            }

            public Song[] newArray(int size) {
                return new Song[size];
            }
        };
}
