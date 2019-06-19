package com.lunchareas.echomp.models;


import android.os.Parcel;
import android.os.Parcelable;

public class Album implements Parcelable {

    private long id;
    private String name;
    private String artist;
    private long artistId;
    private int count;

    public Album(long id, String name, String artist, long artistId, int count) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.artistId = artistId;
        this.count = count;
    }

    public Album(Parcel parcel) {
        this.id = parcel.readLong();
        this.name = parcel.readString();
        this.artist = parcel.readString();
        this.artistId = parcel.readLong();
        this.count = parcel.readInt();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "Name: " + name + "\n" + "ID: " + Long.toString(id) + "\n";
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(id);
        parcel.writeString(name);
        parcel.writeString(artist);
        parcel.writeLong(artistId);
        parcel.writeInt(count);
    }

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {

                public Album createFromParcel(Parcel source) {
                    return new Album(source);
                }

                public Album[] newArray(int size) {
                    return new Album[size];
                }
            };
}
