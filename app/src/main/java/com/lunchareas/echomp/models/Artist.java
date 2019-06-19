package com.lunchareas.echomp.models;


import android.os.Parcel;
import android.os.Parcelable;

public class Artist implements Parcelable {

    private long id;
    private String name;
    private int albumCount;
    private int trackCount;

    public Artist(long id, String name, int albumCount, int trackCount) {
        this.id = id;
        this.name = name;
        this.albumCount = albumCount;
        this.trackCount = trackCount;
    }

    public Artist(Parcel parcel) {
        this.id = parcel.readLong();
        this.name = parcel.readString();
        this.albumCount = parcel.readInt();
        this.trackCount = parcel.readInt();
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

    public int getAlbumCount() {
        return albumCount;
    }

    public void setAlbumCount(int albumCount) {
        this.albumCount = albumCount;
    }

    public int getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(int trackCount) {
        this.trackCount = trackCount;
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
        parcel.writeInt(albumCount);
        parcel.writeInt(trackCount);
    }

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {

                public Artist createFromParcel(Parcel source) {
                    return new Artist(source);
                }

                public Artist[] newArray(int size) {
                    return new Artist[size];
                }
            };
}
