package com.lunchareas.echomp.models;


import android.os.Parcel;
import android.os.Parcelable;

public class Playlist implements Parcelable {

    private long id;
    private String name;
    private int count;

    public Playlist(long id, String name, int count) {
        this.id = id;
        this.name = name;
        this.count = count;
    }

    public Playlist(Parcel parcel) {
        this.id = parcel.readLong();
        this.name = parcel.readString();
        this.count = parcel.readInt();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(id);
        parcel.writeString(name);
        parcel.writeInt(count);
    }

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {

                public Playlist createFromParcel(Parcel source) {
                    return new Playlist(source);
                }

                public Playlist[] newArray(int size) {
                    return new Playlist[size];
                }
            };
}
