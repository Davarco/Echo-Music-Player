package com.lunchareas.divertio.utils;


import android.content.Context;
import android.util.Log;

import com.lunchareas.divertio.models.PlaylistDBHandler;
import com.lunchareas.divertio.models.PlaylistData;
import com.lunchareas.divertio.models.SongData;

import java.util.ArrayList;
import java.util.List;

public class PlaylistUtil {

    private static final String TAG = PlaylistUtil.class.getName();

    private Context context;

    public PlaylistUtil(Context context) {
        this.context = context;
    }

    public void createPlaylist(PlaylistData playlistData) {

        // Create the handler and update
        PlaylistDBHandler db = new PlaylistDBHandler(context);
        db.addPlaylistData(playlistData);
    }

    public void createPlaylist(String name, List<SongData> songList) {

        // Create the handler and update
        PlaylistDBHandler db = new PlaylistDBHandler(context);
        db.addPlaylistData(new PlaylistData(name, songList));
    }

    public void addSongToPlaylist(SongData songData, PlaylistData playlistData) {

        // Get the database
        PlaylistDBHandler db = new PlaylistDBHandler(context);

        // Update the database
        playlistData.getSongList().add(songData);
        db.updatePlaylistData(playlistData);
    }

    public void addSongsToPlaylist(List<SongData> songDataList, PlaylistData playlistData) {

        // Get the database
        PlaylistDBHandler db = new PlaylistDBHandler(context);

        // Update the database
        playlistData.getSongList().addAll(songDataList);
        db.updatePlaylistData(playlistData);
    }

    public void deleteSongsFromPlaylist(List<SongData> songList, PlaylistData playlistData) {

        // Get the database
        PlaylistDBHandler db = new PlaylistDBHandler(context);

        // Update the database
        playlistData.getSongList().removeAll(songList);
        db.updatePlaylistData(playlistData);
    }

    public void changePlaylistName(PlaylistData playlistData, String newTitle) {

        // Create new playlist data
        String prevTitle = playlistData.getPlaylistName();
        playlistData.setPlaylistName(newTitle);
        Log.d(TAG, "Changing title from " + prevTitle + " to " + newTitle);

        // Update the playlist data
        PlaylistDBHandler db = new PlaylistDBHandler(context);
        db.updatePlaylistData(playlistData, prevTitle);
    }

    public void deletePlaylist(PlaylistData playlistData) {

        // Delete the playlist
        PlaylistDBHandler db = new PlaylistDBHandler(context);
        db.deletePlaylistData(playlistData);
    }

    public void removeDuplicateSongs(PlaylistData playlistData) {

        // Get the playlist
        PlaylistDBHandler db = new PlaylistDBHandler(context);
        List<SongData> songList = new ArrayList<>();
        List<SongData> prevList = playlistData.getSongList();

        // Add non-duplicates
        for (SongData songData: prevList) {
            if (!songList.contains(songData)) {
                songList.add(songData);
            }
        }

        // Debug number of duplicates
        Log.d(TAG, "There were " + Integer.toString(prevList.size()-songList.size()) + " duplicates in the playlist.");

        // Update database
        playlistData.setSongList(songList);
        db.updatePlaylistData(playlistData);
    }

    public boolean nameAlreadyExists(String name) {

        // Get playlists
        PlaylistDBHandler db = new PlaylistDBHandler(context);
        List<PlaylistData> playlistDataList = db.getPlaylistDataList();

        // Search through playlists and see if name exists
        for (PlaylistData playlistData: playlistDataList) {
            if (name.equals(playlistData.getPlaylistName())) {
                return true;
            }
        }

        return false;
    }
}
