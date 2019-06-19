package com.lunchareas.echomp.databases;

public class TopTracksPair {

    private long id;
    private int played;

    public TopTracksPair(long id, int played) {
        this.id = id;
        this.played = played;
    }

    public long getKey() {
        return this.id;
    }

    public int getVal() {
        return this.played;
    }
}