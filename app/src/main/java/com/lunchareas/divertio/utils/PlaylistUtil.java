package com.lunchareas.divertio.utils;


import android.content.Context;

import com.lunchareas.divertio.models.PlaylistDBHandler;
import com.lunchareas.divertio.models.PlaylistData;
import com.lunchareas.divertio.models.SongData;

import java.util.List;

public class PlaylistUtil {

    private static final String TAG = PlaylistUtil.class.getName();

    private Context context;

    public PlaylistUtil(Context context) {
        this.context = context;
    }

    public void addSongToPlaylist(SongData songData, PlaylistData playlistData) {

        // Get the database
        PlaylistDBHandler db = new PlaylistDBHandler(context);

        // Update the database
        playlistData.getSongList().add(songData);
        db.updatePlaylistData(playlistData);
    }
}
