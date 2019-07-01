/*
Echo Music Player
Copyright (C) 2019 David Zhang

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
