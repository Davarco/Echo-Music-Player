package com.lunchareas.divertio.utils;

import android.content.Context;
import android.util.Log;

import com.lunchareas.divertio.models.PlaylistDBHandler;
import com.lunchareas.divertio.models.PlaylistData;
import com.lunchareas.divertio.models.SongDBHandler;
import com.lunchareas.divertio.models.SongData;

import java.io.File;
import java.util.List;

public class SongUtil {

    private static final String TAG = SongUtil.class.getName();

    private Context context;

    public SongUtil(Context context) {
        this.context = context;
    }

    public void changeSongName(SongData songData, String newTitle) {

        // Update all the playlists
        PlaylistUtil playlistUtil = new PlaylistUtil(context);
        playlistUtil.replaceSongInPlaylists(songData, new SongData(newTitle, songData.getSongPath(), songData.getSongArtist()));

        // Create new song data with title
        String prevName = songData.getSongName();
        songData.setSongName(newTitle);
        Log.d(TAG, "Changing title from " + prevName + " to " + newTitle);

        // Update the song data
        SongDBHandler db = new SongDBHandler(context);
        db.updateSongData(songData, prevName);
    }

    public void changeSongArtist(SongData songData, String newArtist) {

        // Update all the playlists
        PlaylistUtil playlistUtil = new PlaylistUtil(context);
        playlistUtil.replaceSongInPlaylists(songData, new SongData(songData.getSongName(), songData.getSongPath(), newArtist));

        // Create new song data with artist
        String prevArtist = songData.getSongArtist();
        songData.setSongArtist(newArtist);
        Log.d(TAG, "Changing title from " + prevArtist + " to " + newArtist);

        // Update the song data
        SongDBHandler db = new SongDBHandler(context);
        db.updateSongData(songData);
    }

    public void deleteSongList(List<SongData> songList) {

        // Call delete song on all of them
        for (SongData songData: songList) {
            deleteSong(songData);
        }
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
                    Log.d(TAG," Removing song no " + Integer.toString(j+1));
                    songList.remove(j);
                }
            }
            PlaylistData newPlaylist = new PlaylistData(playlistDataList.get(i).getPlaylistName(), songList);
            Log.d(TAG, newPlaylist.toString());
            db.updatePlaylistData(newPlaylist);
        }
    }

    public boolean nameAlreadyExists(String input) {

        // Get songs
        SongDBHandler db = new SongDBHandler(context);
        List<SongData> songList = db.getSongDataList();

        // Search through and see if the song exists
        for (SongData songData: songList) {
            if (input.equals(songData.getSongName())) {
                return true;
            }
        }

        return false;
    }
}
