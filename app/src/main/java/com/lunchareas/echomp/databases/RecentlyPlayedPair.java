package com.lunchareas.echomp.databases;

public class RecentlyPlayedPair {

    private long id;
    private int played;

    public RecentlyPlayedPair(long id, int played) {
        this.id = id;
        this.played = played;
    }

    public long getKey() {
        return this.id;
    }

    public int getVal() {
        return this.played;
    }

    @Override
    public boolean equals(Object other) {
        RecentlyPlayedPair otherPair = (RecentlyPlayedPair) other;
        return (otherPair.getKey() == this.getKey());
    }
}