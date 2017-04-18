package com.lunchareas.divertio.utils;

import android.content.Context;
import android.util.Log;

import com.lunchareas.divertio.models.PlaylistDBHandler;
import com.lunchareas.divertio.models.PlaylistData;
import com.lunchareas.divertio.models.SongDBHandler;
import com.lunchareas.divertio.models.SongData;

import java.io.File;
import java.util.List;

public class MusicManager {

    private static final String TAG = MusicManager.class.getName();

    private Context context;

    public MusicManager(Context context) {
        this.context = context;
    }

    public void deleteSong(SongData songData) {
        // Make sure all playlists no longer have song
        removeSongFromPlaylists(songData);

        // Remove the song and the file
        SongDBHandler db = new SongDBHandler(context);
        new File(db.getSongData(songData.getSongName()).getSongPath()).delete();
        db.deleteSongData(songData);
    }

    public void removeSongFromPlaylists(SongData songData) {

        // Get list of playlists
        PlaylistDBHandler db = new PlaylistDBHandler(context);
        List<PlaylistData> playlistDataList = db.getPlaylistDataList();

        // Look through all of the playlists and delete song
        for (int i = 0; i < playlistDataList.size(); i++) {
            List<SongData> songList = playlistDataList.get(i).getSongList();
            for (int j = 0; j < songList.size(); j++) {
                if (songList.get(j).equals(songData)) {
                    System.out.println("Removing song no " + Integer.toString(j+1));
                    songList.remove(j);
                }
            }
            PlaylistData newPlaylist = new PlaylistData(playlistDataList.get(i).getPlaylistName(), songList);
            Log.d(TAG, newPlaylist.toString());
            db.updatePlaylistData(newPlaylist);
        }
    }
}
